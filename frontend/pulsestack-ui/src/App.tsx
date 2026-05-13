import { useState, useEffect } from 'react';
import { Sidebar } from './components/Sidebar';
import { ChannelFeed } from './components/ChannelFeed';

const API_URL = 'http://localhost:8081/api/v1/channels';

interface ChannelData {
  id: string;
  name: string;
  displayName: string;
  description?: string;
}

export default function App() {
  const [channels, setChannels] = useState<ChannelData[]>([]);
  const [activeChannelName, setActiveChannelName] = useState<string>('java');

  useEffect(() => {
    fetch(API_URL)
      .then(res => res.json())
      .then(setChannels)
      .catch(() => {
        // Fallback: Channels ohne IDs — WebSocket wird nicht verbunden
        console.warn('Could not load channels from API');
      });
  }, []);

  const activeChannel = channels.find(c => c.name === activeChannelName);

  return (
    <div className="flex h-screen bg-gray-950 text-white overflow-hidden">
      <Sidebar
        activeChannelName={activeChannelName}
        onChannelSelect={setActiveChannelName}
      />
      <ChannelFeed
        channelId={activeChannel?.id ?? null}
        channelName={activeChannelName}
      />
    </div>
  );
}
