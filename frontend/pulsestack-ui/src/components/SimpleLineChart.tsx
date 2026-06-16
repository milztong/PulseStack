/**
 * Minimaler, abhängigkeitsfreier Line-Chart (kein recharts/d3).
 *
 * Hintergrund: recharts zieht d3-Internals nach sich, deren minifizierter Code
 * auf dem Linux-Build (Vercel/Rolldown) wiederholt mit TDZ/Hoisting-Fehlern
 * ("X is not a function") gecrasht ist — auch nach Lazy-Loading und
 * manueller Chunk-Konsolidierung. Für einfache Linien reicht natives SVG.
 */
interface SimpleLineChartProps {
  data: { date: string; close: number }[];
  height?: number;
  color?: string;
}

export function SimpleLineChart({ data, height = 220, color = '#818cf8' }: SimpleLineChartProps) {
  if (data.length === 0) {
    return (
      <div style={{ height }} className="flex items-center justify-center text-gray-500 text-sm">
        Keine Daten verfügbar.
      </div>
    );
  }

  const width = 600; // viewBox-Breite, skaliert via SVG responsiv
  const paddingX = 8;
  const paddingY = 12;

  const closes = data.map(d => d.close);
  const min = Math.min(...closes);
  const max = Math.max(...closes);
  const range = max - min || 1;

  const points = data.map((d, i) => {
    const x = paddingX + (i / Math.max(data.length - 1, 1)) * (width - paddingX * 2);
    const y = paddingY + (1 - (d.close - min) / range) * (height - paddingY * 2);
    return `${x},${y}`;
  });

  const first = data[0];
  const last = data[data.length - 1];

  return (
    <div>
      <svg viewBox={`0 0 ${width} ${height}`} width="100%" height={height} preserveAspectRatio="none">
        <polyline
          points={points.join(' ')}
          fill="none"
          stroke={color}
          strokeWidth={2}
          strokeLinejoin="round"
          strokeLinecap="round"
        />
      </svg>
      <div className="flex justify-between text-xs text-gray-500 mt-1">
        <span>{first.date}</span>
        <span>{last.date}</span>
      </div>
    </div>
  );
}
