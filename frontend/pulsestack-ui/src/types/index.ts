export interface NewsItem {
  externalId: string;
  source: 'REDDIT' | 'YOUTUBE' | 'GITHUB' | 'NEWSAPI';
  channelId: string;
  title: string;
  url: string;
  thumbnailUrl?: string;
  author?: string;
  score?: number;
  publishedAt: string;
  fetchedAt: string;
}

export interface Channel {
  id: string;
  name: string;
  displayName: string;
  description?: string;
}