import { useState, useEffect } from 'react';
import { useChannelFeed } from '../hooks/useChannelFeed';
import { NewsCard } from './NewsCard';
import { CHANNELS } from '../data/channels';
import type { NewsItem } from '../types';

interface Props {
  channelId: string | null;
  channelName: string;
}

export function ChannelFeed({ channelId, channelName }: Props) {
  const { items: liveItems, connected } = useChannelFeed(channelId);
  const [historyItems, setHistoryItems] = useState<NewsItem[]>([]);
  const channel = CHANNELS.find(c => c.name === channelName);

  useEffect(() => {
    if (!channelId) return;
    fetch(`http://localhost:8083/api/v1/news/channel/${channelId}`)
      .then(res => res.json())
      .then(setHistoryItems)
      .catch(() => setHistoryItems([]));
  }, [channelId]);

  const allItems = [...liveItems, ...historyItems].filter(
    (item, index, self) => self.findIndex(i => i.externalId === item.externalId) === index
  );

  if (!channelId) {
    return (
      <div className="flex-1 flex items-center justify-center text-white/30">
        Wähle einen Channel
      </div>
    );
  }

  return (
    <div className="flex-1 flex flex-col h-screen overflow-hidden">
      <div className="p-4 border-b border-white/10 flex items-center justify-between shrink-0">
        <div>
          <h2 className="text-white font-semibold"># {channelName}</h2>
          {channel?.description && (
            <p className="text-xs text-white/40 mt-0.5">{channel.description}</p>
          )}
        </div>
        <div className="flex items-center gap-2">
          <div className={`w-2 h-2 rounded-full ${connected ? 'bg-green-400' : 'bg-red-400'}`} />
          <span className="text-xs text-white/40">{connected ? 'Live' : 'Disconnected'}</span>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-3">
        {allItems.length === 0 ? (
          <div className="flex items-center justify-center h-full text-white/30 text-sm">
            Warte auf neue Items…
          </div>
        ) : (
          allItems.map(item => (
            <NewsCard key={item.externalId} item={item} />
          ))
        )}
      </div>
    </div>
  );
}