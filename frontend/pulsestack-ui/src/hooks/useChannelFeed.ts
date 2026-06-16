import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import type { IMessage } from '@stomp/stompjs';
import type { NewsItem } from '../types';

const BASE_URL = import.meta.env.VITE_PROCESSING_URL ?? 'http://localhost:8083';
const WEBSOCKET_URL = BASE_URL.replace(/^http/, 'ws') + '/ws';
const MAX_ITEMS = 50;

function getToken(): string | null {
  try {
    const raw = localStorage.getItem('pulsestack_auth');
    if (raw) return JSON.parse(raw).token ?? null;
  } catch { /* ignore */ }
  return null;
}

export function useChannelFeed(channelId: string | null) {
  const [items, setItems] = useState<NewsItem[]>([]);
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!channelId) return;

    const token = getToken();

    // Vorhandene Items per REST laden, bevor wir auf neue Live-Items warten
    fetch(`${BASE_URL}/api/v1/news/channel/${channelId}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
      .then(res => (res.ok ? res.json() : []))
      .then((existing: NewsItem[]) => setItems(existing.slice(0, MAX_ITEMS)))
      .catch(() => console.warn('Could not load existing news items'));

    const client = new Client({
      brokerURL: WEBSOCKET_URL,
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      onConnect: () => {
        setConnected(true);
        client.subscribe(`/topic/channel/${channelId}`, (message: IMessage) => {
          const newsItem: NewsItem = JSON.parse(message.body);
          setItems(prev => [newsItem, ...prev].slice(0, MAX_ITEMS));
        });
      },
      onDisconnect: () => setConnected(false),
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message']);
        setConnected(false);
      },
      reconnectDelay: 5000,
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      setItems([]);
      setConnected(false);
    };
  }, [channelId]);

  return { items, connected };
}
