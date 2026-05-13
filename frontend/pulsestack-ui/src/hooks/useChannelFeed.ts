import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import type { IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { NewsItem } from '../types';

const WEBSOCKET_URL = 'http://localhost:8083/ws';
const MAX_ITEMS = 50;

export function useChannelFeed(channelId: string | null) {
  const [items, setItems] = useState<NewsItem[]>([]);
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!channelId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(WEBSOCKET_URL),
      onConnect: () => {
        setConnected(true);
        client.subscribe(`/topic/channel/${channelId}`, (message: IMessage) => {
          const newsItem: NewsItem = JSON.parse(message.body);
          setItems(prev => [newsItem, ...prev].slice(0, MAX_ITEMS));
        });
      },
      onDisconnect: () => setConnected(false),
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