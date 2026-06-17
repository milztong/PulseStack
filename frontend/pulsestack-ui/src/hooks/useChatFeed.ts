import { useEffect, useRef, useState, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import type { IMessage } from '@stomp/stompjs';
import type { ChatMessage } from '../types/Chat';

const BASE_URL = import.meta.env.VITE_CHAT_URL ?? 'http://localhost:8082';
const CHAT_WS_URL = BASE_URL.replace(/^http/, 'ws') + '/ws';
const CHAT_API_URL = BASE_URL + '/api/v1/chat';
const MAX_MESSAGES = 100;
const HEARTBEAT_INTERVAL_MS = 30_000;
const TYPING_DEBOUNCE_MS = 1_500;

function getToken(): string | null {
  try {
    const raw = localStorage.getItem('pulsestack_auth');
    if (raw) return JSON.parse(raw).token ?? null;
  } catch { /* ignore */ }
  return null;
}

export function useChatFeed(channelId: string | null, username: string | null) {
  const [messages, setMessages]       = useState<ChatMessage[]>([]);
  const [connected, setConnected]     = useState(false);
  const [typingUsers, setTypingUsers] = useState<string[]>([]);
  const clientRef   = useRef<Client | null>(null);
  const heartbeatRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const typingTimers = useRef<Record<string, ReturnType<typeof setTimeout>>>({});

  const sendMessage = useCallback((content: string) => {
    if (!clientRef.current?.connected || !channelId || !content.trim()) return;
    clientRef.current.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ channelId, content: content.trim() }),
    });
  }, [channelId]);

  const sendTyping = useCallback(() => {
    if (!clientRef.current?.connected || !channelId || !username) return;
    clientRef.current.publish({
      destination: '/app/chat.typing',
      body: JSON.stringify({ channelId, username }),
    });
  }, [channelId, username]);

  useEffect(() => {
    if (!channelId) return;

    const token = getToken();

    fetch(`${CHAT_API_URL}/channel/${channelId}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
      .then(r => r.json())
      .then((history: ChatMessage[]) => setMessages(history.reverse()))
      .catch(() => setMessages([]));

    const client = new Client({
      brokerURL: CHAT_WS_URL,
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},

      onConnect: () => {
        setConnected(true);

        client.subscribe(`/topic/chat/${channelId}`, (msg: IMessage) => {
          const chatMsg: ChatMessage = JSON.parse(msg.body);
          setMessages(prev => [...prev, chatMsg].slice(-MAX_MESSAGES));
        });

        client.subscribe(`/topic/typing/${channelId}`, (msg: IMessage) => {
          const typer = msg.body.replace(/^"|"$/g, '');
          if (typer === username) return;

          setTypingUsers(prev => prev.includes(typer) ? prev : [...prev, typer]);

          clearTimeout(typingTimers.current[typer]);
          typingTimers.current[typer] = setTimeout(() => {
            setTypingUsers(prev => prev.filter(u => u !== typer));
          }, TYPING_DEBOUNCE_MS);
        });

        heartbeatRef.current = setInterval(() => {
          if (client.connected) {
            client.publish({ destination: '/app/presence.heartbeat', body: JSON.stringify(channelId) });
          }
        }, HEARTBEAT_INTERVAL_MS);
        client.publish({ destination: '/app/presence.heartbeat', body: JSON.stringify(channelId) });
      },

      onDisconnect: () => {
        setConnected(false);
        if (heartbeatRef.current) clearInterval(heartbeatRef.current);
      },

      onStompError: (frame) => {
        console.error('Chat STOMP error:', frame.headers['message']);
        setConnected(false);
      },

      reconnectDelay: 5000,
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (heartbeatRef.current) clearInterval(heartbeatRef.current);
      Object.values(typingTimers.current).forEach(clearTimeout);
      client.deactivate();
      setMessages([]);
      setTypingUsers([]);
      setConnected(false);
    };
  }, [channelId, username]);

  return { messages, connected, typingUsers, sendMessage, sendTyping };
}
