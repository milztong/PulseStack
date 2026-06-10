import type { NewsItem } from '../types';

const SOURCE_STYLES = {
  REDDIT:  'bg-orange-500/20 text-orange-400 border-orange-500/30',
  YOUTUBE: 'bg-red-500/20 text-red-400 border-red-500/30',
  GITHUB:  'bg-purple-500/20 text-purple-400 border-purple-500/30',
  NEWSAPI: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
} as const;

const SOURCE_LABELS = {
  REDDIT:  'Reddit',
  YOUTUBE: 'YouTube',
  GITHUB:  'GitHub',
  NEWSAPI: 'News',
} as const;

interface Props {
  source: NewsItem['source'];
}

export function SourceBadge({ source }: Props) {
  return (
    <span className={`text-xs px-2 py-0.5 rounded-full border font-medium ${SOURCE_STYLES[source]}`}>
      {SOURCE_LABELS[source]}
    </span>
  );
}