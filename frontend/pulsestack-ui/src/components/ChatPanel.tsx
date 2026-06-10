import { useEffect, useRef, useState } from 'react';
import { useChatFeed } from '../hooks/useChatFeed';
import { ChatMessageCard } from './ChatMessageCard';

interface Props {
  channelId: string | null;
  username: string | null;
}

export function ChatPanel({ channelId, username }: Props) {
  const { messages, connected, typingUsers, sendMessage, sendTyping } =
    useChatFeed(channelId, username);

  const [input, setInput]       = useState('');
  const [typingSent, setTypingSent] = useState(false);
  const bottomRef               = useRef<HTMLDivElement>(null);

  // Auto-scroll zu neuesten Nachrichten
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  function handleInput(value: string) {
    setInput(value);
    // Typing-Event nur einmal pro "Tipp-Session" senden (kein Spam)
    if (value.length > 0 && !typingSent) {
      sendTyping();
      setTypingSent(true);
      setTimeout(() => setTypingSent(false), 2000);
    }
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!input.trim()) return;
    sendMessage(input);
    setInput('');
    setTypingSent(false);
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLTextAreaElement>) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e as unknown as React.FormEvent);
    }
  }

  if (!channelId) {
    return (
      <div className="w-80 border-l border-gray-800 flex items-center justify-center text-gray-600 text-sm">
        Wähle einen Channel
      </div>
    );
  }

  return (
    <div className="w-80 border-l border-gray-800 flex flex-col bg-gray-950">
      {/* Header */}
      <div className="px-4 py-3 border-b border-gray-800 flex items-center justify-between shrink-0">
        <span className="text-sm font-semibold text-white">Chat</span>
        <div className="flex items-center gap-1.5">
          <div className={`w-1.5 h-1.5 rounded-full ${connected ? 'bg-green-400' : 'bg-red-500'}`} />
          <span className="text-xs text-gray-500">{connected ? 'Verbunden' : 'Getrennt'}</span>
        </div>
      </div>

      {/* Nachrichten */}
      <div className="flex-1 overflow-y-auto p-3 flex flex-col gap-3">
        {messages.length === 0 ? (
          <div className="flex items-center justify-center h-full text-gray-600 text-xs">
            Noch keine Nachrichten
          </div>
        ) : (
          messages.map(msg => (
            <ChatMessageCard
              key={msg.id}
              message={msg}
              isOwn={msg.senderName === username}
            />
          ))
        )}
        <div ref={bottomRef} />
      </div>

      {/* Typing-Indikator */}
      <div className="px-4 h-5 shrink-0">
        {typingUsers.length > 0 && (
          <p className="text-xs text-gray-500 italic">
            {typingUsers.join(', ')} {typingUsers.length === 1 ? 'tippt' : 'tippen'}…
          </p>
        )}
      </div>

      {/* Input */}
      <form onSubmit={handleSubmit} className="p-3 border-t border-gray-800 shrink-0">
        <div className="flex gap-2 items-end">
          <textarea
            value={input}
            onChange={e => handleInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Nachricht schreiben… (Enter zum Senden)"
            rows={1}
            className="flex-1 resize-none bg-gray-800 text-white text-sm placeholder-gray-600
                       rounded-lg px-3 py-2 focus:outline-none focus:ring-1 focus:ring-indigo-500
                       max-h-24 overflow-y-auto"
            style={{ fieldSizing: 'content' } as React.CSSProperties}
          />
          <button
            type="submit"
            disabled={!input.trim() || !connected}
            className="p-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-40
                       disabled:cursor-not-allowed rounded-lg transition-colors shrink-0"
            aria-label="Senden"
          >
            <svg className="w-4 h-4 text-white" viewBox="0 0 24 24" fill="currentColor">
              <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
            </svg>
          </button>
        </div>
      </form>
    </div>
  );
}
