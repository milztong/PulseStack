import { useEffect, useState } from 'react';

interface TrendDataPoint {
  channelId: string;
  channelName: string;
  reddit: number;
  youtube: number;
  github: number;
  newsapi: number;
}

const ANALYTICS_URL = `${import.meta.env.VITE_PROCESSING_URL ?? 'http://localhost:8083'}/api/v1/analytics/trends`;

const SOURCES = [
  { key: 'reddit',  label: 'Reddit',  color: '#ff4500' },
  { key: 'youtube', label: 'YouTube', color: '#ff0000' },
  { key: 'github',  label: 'GitHub',  color: '#6e40c9' },
  { key: 'newsapi', label: 'NewsAPI', color: '#0ea5e9' },
] as const;

interface ChartRow {
  name: string;
  reddit: number;
  youtube: number;
  github: number;
  newsapi: number;
}

function SimpleBarChart({ data }: { data: ChartRow[] }) {
  if (data.length === 0) return null;

  const W = 800;
  const H = 360;
  const MARGIN = { top: 16, right: 16, bottom: 80, left: 40 };
  const innerW = W - MARGIN.left - MARGIN.right;
  const innerH = H - MARGIN.top - MARGIN.bottom;

  const maxVal = Math.max(1, ...data.flatMap(d => SOURCES.map(s => d[s.key])));
  const groupW = innerW / data.length;
  const barW = Math.max(4, (groupW - 8) / SOURCES.length);

  const yTicks = 5;
  const yStep = Math.ceil(maxVal / yTicks);

  return (
    <svg viewBox={`0 0 ${W} ${H}`} className="w-full" style={{ maxHeight: 400 }}>
      <g transform={`translate(${MARGIN.left},${MARGIN.top})`}>
        {/* grid lines */}
        {Array.from({ length: yTicks + 1 }, (_, i) => {
          const val = i * yStep;
          const y = innerH - (val / (yStep * yTicks)) * innerH;
          return (
            <g key={i}>
              <line x1={0} y1={y} x2={innerW} y2={y} stroke="#374151" strokeDasharray="3 3" />
              <text x={-6} y={y + 4} textAnchor="end" fill="#9ca3af" fontSize={11}>{val}</text>
            </g>
          );
        })}

        {/* bars */}
        {data.map((row, gi) => {
          const groupX = gi * groupW;
          return (
            <g key={row.name}>
              {SOURCES.map((src, si) => {
                const val = row[src.key];
                const barH = (val / (yStep * yTicks)) * innerH;
                const x = groupX + si * barW + 4;
                const y = innerH - barH;
                return (
                  <g key={src.key}>
                    <rect x={x} y={y} width={barW - 1} height={barH} fill={src.color} rx={2} />
                    {val > 0 && (
                      <text x={x + barW / 2} y={y - 3} textAnchor="middle" fill={src.color} fontSize={9}>{val}</text>
                    )}
                  </g>
                );
              })}
              {/* x-axis label */}
              <text
                x={groupX + groupW / 2}
                y={innerH + 16}
                textAnchor="end"
                fill="#9ca3af"
                fontSize={11}
                transform={`rotate(-35, ${groupX + groupW / 2}, ${innerH + 16})`}
              >
                {row.name.length > 14 ? row.name.slice(0, 13) + '…' : row.name}
              </text>
            </g>
          );
        })}

        {/* x-axis line */}
        <line x1={0} y1={innerH} x2={innerW} y2={innerH} stroke="#4b5563" />
      </g>

      {/* legend */}
      <g transform={`translate(${MARGIN.left}, ${H - 18})`}>
        {SOURCES.map((src, i) => (
          <g key={src.key} transform={`translate(${i * 110}, 0)`}>
            <rect width={10} height={10} fill={src.color} rx={2} />
            <text x={14} y={9} fill="#9ca3af" fontSize={11}>{src.label}</text>
          </g>
        ))}
      </g>
    </svg>
  );
}

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

  const chartData: ChartRow[] = data
    .filter(d => d.reddit + d.youtube + d.github + d.newsapi > 0)
    .map(d => ({
      name: d.channelName,
      reddit: d.reddit,
      youtube: d.youtube,
      github: d.github,
      newsapi: d.newsapi,
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
          <SimpleBarChart data={chartData} />
        </div>
      )}

      {!loading && chartData.length > 0 && (
        <div className="mt-6 grid grid-cols-2 sm:grid-cols-4 gap-4">
          {SOURCES.map((src) => {
            const total = data.reduce((s, d) => s + d[src.key], 0);
            return (
              <div key={src.key} className="bg-gray-900 rounded-xl p-4">
                <p className="text-xs text-gray-400 uppercase tracking-wide">{src.label}</p>
                <p className="text-3xl font-bold mt-1" style={{ color: src.color }}>{total}</p>
                <p className="text-xs text-gray-500 mt-1">items in period</p>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
