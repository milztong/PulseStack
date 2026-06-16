/**
 * Minimaler, abhängigkeitsfreier Line-Chart (kein recharts/d3).
 *
 * Hintergrund: recharts zieht d3-Internals nach sich, deren minifizierter Code
 * auf dem Linux-Build (Vercel/Rolldown) wiederholt mit TDZ/Hoisting-Fehlern
 * ("X is not a function") gecrasht ist — auch nach Lazy-Loading und
 * manueller Chunk-Konsolidierung. Für einfache Linien reicht natives SVG.
 *
 * Optik orientiert sich am Original-StockPredictor-Chart (weiße Linie,
 * dezentes dunkles Grid statt buntem Theme).
 */
interface SimpleLineChartProps {
  data: { date: string; close: number }[];
  height?: number;
}

export function SimpleLineChart({ data, height = 220 }: SimpleLineChartProps) {
  if (data.length === 0) {
    return (
      <div style={{ height }} className="flex items-center justify-center text-neutral-600 text-xs tracking-widest uppercase">
        Keine Daten verfügbar
      </div>
    );
  }

  const width = 600;
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

  const gridLinesY = [0.25, 0.5, 0.75].map(f => paddingY + f * (height - paddingY * 2));

  const first = data[0];
  const last = data[data.length - 1];

  return (
    <div>
      <svg viewBox={`0 0 ${width} ${height}`} width="100%" height={height} preserveAspectRatio="none">
        {gridLinesY.map(y => (
          <line key={y} x1={0} y1={y} x2={width} y2={y} stroke="#1a1a1a" strokeWidth={1} />
        ))}
        <polyline
          points={points.join(' ')}
          fill="none"
          stroke="#ffffff"
          strokeWidth={1.5}
          strokeLinejoin="round"
          strokeLinecap="round"
        />
      </svg>
      <div className="flex justify-between text-xs text-neutral-600 mt-2">
        <span>{first.date}</span>
        <span>{last.date}</span>
      </div>
    </div>
  );
}
