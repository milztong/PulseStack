import type { NewsItem } from '../types';
import { SourceBadge } from './SourceBadge';

interface Props {
  item: NewsItem;
}

function formatTime(isoString: string): string {
  const date = new Date(isoString);
  return date.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' });
}

export function NewsCard({ item }: Props) {
  return (
    
      <a href={item.url}
      target="_blank"
      rel="noopener noreferrer"
      className="block p-4 rounded-lg border border-white/10 bg-white/5 hover:bg-white/10 hover:border-white/20 transition-all duration-200 group"
    >
      <div className="flex items-start justify-between gap-3 mb-2">
        <SourceBadge source={item.source} />
        <span className="text-xs text-white/40 shrink-0">
          {formatTime(item.publishedAt)}
        </span>
      </div>

      <p className="text-sm text-white/90 font-medium leading-snug group-hover:text-white transition-colors line-clamp-2">
        {item.title}
      </p>

      <div className="flex items-center gap-3 mt-2">
        {item.author && (
          <span className="text-xs text-white/40">by {item.author}</span>
        )}
        {item.score !== undefined && item.score > 0 && (
          <span className="text-xs text-white/40">▲ {item.score}</span>
        )}
      </div>
    </a>
  );
}