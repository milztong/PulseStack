import type { ChatMessage } from '../types/Chat';

interface Props {
  message: ChatMessage;
  isOwn: boolean;
}

function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

export function ChatMessageCard({ message, isOwn }: Props) {
  return (
    <div className={`flex flex-col ${isOwn ? 'items-end' : 'items-start'} gap-0.5`}>
      {/* Sender + Timestamp */}
      <div className="flex items-baseline gap-2 px-1">
        <span className={`text-xs font-medium ${isOwn ? 'text-indigo-400' : 'text-emerald-400'}`}>
          {isOwn ? 'You' : message.senderName}
        </span>
        <span className="text-xs text-gray-600">{formatTime(message.sentAt)}</span>
      </div>

      {/* Bubble */}
      <div
        className={`max-w-[85%] px-3 py-2 rounded-2xl text-sm leading-relaxed break-words ${
          isOwn
            ? 'bg-indigo-600 text-white rounded-tr-sm'
            : 'bg-gray-800 text-gray-100 rounded-tl-sm'
        }`}
      >
        {message.content}
      </div>
    </div>
  );
}
