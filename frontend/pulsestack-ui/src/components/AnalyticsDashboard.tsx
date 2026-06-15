import { useEffect, useState } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
} from 'recharts';

interface TrendDataPoint {
  channelId: string;
  channelName: string;
  reddit: number;
  youtube: number;
  github: number;
  newsapi: number;
}

const ANALYTICS_URL = `${import.meta.env.VITE_PROCESSING_URL ?? 'http://localhost:8083'}/api/v1/analytics/trends`;

const SOURCE_COLORS = {
  reddit:  '#ff4500',
  youtube: '#ff0000',
  github:  '#6e40c9',
  newsapi: '#0ea5e9',
};

export function AnalyticsDashboard() {
  const [data, setData] = useState<TrendDataPoint[]>([]);
  const [days, setDays] = useState(7);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);
    setError('');
    fetch(`${ANALYTICS_URL}?days=${days}`)
      .then(r => r.json())
      .then(setData)
      .catch(() => setError('Could not load analytics. Is processing-service running?'))
      .finally(() => setLoading(false));
  }, [days]);

  const chartData = data
    .filter(d => d.reddit + d.youtube + d.github + d.newsapi > 0)
    .map(d => ({
      name: d.channelName,
      Reddit: d.reddit,
      YouTube: d.youtube,
      GitHub: d.github,
      NewsAPI: d.newsapi,
    }));

  return (
    <div className="flex-1 overflow-y-auto p-6 bg-gray-950 text-white">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold">Trend Analytics</h2>
          <p className="text-gray-400 text-sm mt-1">Cross-platform item counts per channel</p>
        </div>
        <select
          value={days}
          onChange={e => setDays(Number(e.target.value))}
          className="bg-gray-800 border border-gray-700 text-white rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-indigo-500"
        >
          <option value={1}>Last 24h</option>
          <option value={7}>Last 7 days</option>
          <option value={14}>Last 14 days</option>
          <option value={30}>Last 30 days</option>
        </select>
      </div>

      {loading && <p className="text-gray-400">Loading…</p>}
      {error && <p className="text-red-400">{error}</p>}

      {!loading && !error && chartData.length === 0 && (
        <div className="flex flex-col items-center justify-center h-64 text-gray-500">
          <p className="text-lg">No data yet for this period.</p>
          <p className="text-sm mt-1">Run the ingestion service to populate trends.</p>
        </div>
      )}

      {!loading && chartData.length > 0 && (
        <div className="bg-gray-900 rounded-2xl p-4">
          <ResponsiveContainer width="100%" height={420}>
            <BarChart data={chartData} margin={{ top: 8, right: 16, left: 0, bottom: 60 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
              <XAxis
                dataKey="name"
                tick={{ fill: '#9ca3af', fontSize: 12 }}
                angle={-40}
                textAnchor="end"
                interval={0}
              />
              <YAxis tick={{ fill: '#9ca3af', fontSize: 12 }} allowDecimals={false} />
              <Tooltip
                contentStyle={{ backgroundColor: '#1f2937', border: 'none', borderRadius: '8px' }}
                labelStyle={{ color: '#f9fafb', fontWeight: 600 }}
                itemStyle={{ color: '#d1d5db' }}
              />
              <Legend wrapperStyle={{ paddingTop: '16px', color: '#9ca3af' }} />
              <Bar dataKey="Reddit"  fill={SOURCE_COLORS.reddit}  radius={[3,3,0,0]} />
              <Bar dataKey="YouTube" fill={SOURCE_COLORS.youtube} radius={[3,3,0,0]} />
              <Bar dataKey="GitHub"  fill={SOURCE_COLORS.github}  radius={[3,3,0,0]} />
              <Bar dataKey="NewsAPI" fill={SOURCE_COLORS.newsapi} radius={[3,3,0,0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {!loading && chartData.length > 0 && (
        <div className="mt-6 grid grid-cols-2 sm:grid-cols-4 gap-4">
          {(['Reddit','YouTube','GitHub','NewsAPI'] as const).map((src, i) => {
            const key = src.toLowerCase() as keyof typeof SOURCE_COLORS;
            const total = data.reduce((s, d) => s + (d[key as keyof TrendDataPoint] as number), 0);
            return (
              <div key={src} className="bg-gray-900 rounded-xl p-4">
                <p className="text-xs text-gray-400 uppercase tracking-wide">{src}</p>
                <p className="text-3xl font-bold mt-1" style={{ color: SOURCE_COLORS[key] }}>{total}</p>
                <p className="text-xs text-gray-500 mt-1">items in period</p>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
