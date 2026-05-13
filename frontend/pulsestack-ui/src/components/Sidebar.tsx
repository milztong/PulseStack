import { CHANNELS } from '../data/channels';

interface Props {
  activeChannelName: string;
  onChannelSelect: (channelName: string) => void;
}

export function Sidebar({ activeChannelName, onChannelSelect }: Props) {
  return (
    <aside className="w-56 shrink-0 bg-black/40 border-r border-white/10 flex flex-col h-screen">
      <div className="p-4 border-b border-white/10">
        <h1 className="text-lg font-bold text-white tracking-tight">
          Pulse<span className="text-blue-400">Stack</span>
        </h1>
        <p className="text-xs text-white/40 mt-0.5">Real-time trends</p>
      </div>

      <nav className="flex-1 overflow-y-auto py-2">
        {CHANNELS.map(channel => (
          <button
            key={channel.name}
            onClick={() => onChannelSelect(channel.name)}
            className={`w-full text-left px-4 py-2 text-sm transition-colors ${
              activeChannelName === channel.name
                ? 'bg-blue-500/20 text-blue-300 font-medium'
                : 'text-white/60 hover:text-white/90 hover:bg-white/5'
            }`}
          >
            # {channel.name}
          </button>
        ))}
      </nav>
    </aside>
  );
}