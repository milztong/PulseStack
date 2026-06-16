/**
 * Minimaler, abhängigkeitsfreier Line-Chart (kein recharts/d3).
 *
 * Hintergrund: recharts zieht d3-Internals nach sich, deren minifizierter Code
 * auf dem Linux-Build (Vercel/Rolldown) wiederholt mit TDZ/Hoisting-Fehlern
 * ("X is not a function") gecrasht ist — auch nach Lazy-Loading und
 * manueller Chunk-Konsolidierung. Für einfache Linien reicht natives SVG.
 *
 * Optik orientiert sich am Original-StockPredictor-Chart (weiße Linie,
 * dezentes dunkles Grid, Y-Achsen-Beschriftung mit Preiswerten).
 */
interface SimpleLineChartProps {
  data: { date: string; close: number }[];
  height?: number;
}

const Y_AXIS_WIDTH = 56;

export function SimpleLineChart({ data, height = 220 }: SimpleLineChartProps) {
  if (data.length === 0) {
    return (
      <div style={{ height }} className="flex items-center justify-center text-neutral-600 text-xs tracking-widest uppercase">
        Keine Daten verfügbar
      </div>
    );
  }

  const width = 600;
  const plotWidth = width - Y_AXIS_WIDTH;
  const paddingX = 8;
  const paddingY = 12;

  const closes = data.map(d => d.close);
  const min = Math.min(...closes);
  const max = Math.max(...closes);
  const range = max - min || 1;

  const points = data.map((d, i) => {
    const x = paddingX + (i / Math.max(data.length - 1, 1)) * (plotWidth - paddingX * 2);
    const y = paddingY + (1 - (d.close - min) / range) * (height - paddingY * 2);
    return `${x},${y}`;
  });

  // 4 horizontale Referenzlinien inkl. Wert-Beschriftung, wie im Original
  const steps = [0, 0.25, 0.5, 0.75, 1];
  const gridLines = steps.map(f => ({
    y: paddingY + f * (height - paddingY * 2),
    value: max - f * range,
  }));

  const first = data[0];
  const last = data[data.length - 1];

  return (
    <div>
      <div className="flex">
        <svg viewBox={`0 0 ${plotWidth} ${height}`} width={`calc(100% - ${Y_AXIS_WIDTH}px)`} height={height} preserveAspectRatio="none">
          {gridLines.map(g => (
            <line key={g.y} x1={0} y1={g.y} x2={plotWidth} y2={g.y} stroke="#1a1a1a" strokeWidth={1} />
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
        {/* Y-Achse mit Preiswerten */}
        <div className="relative shrink-0" style={{ width: Y_AXIS_WIDTH, height }}>
          {gridLines.map(g => (
            <span
              key={g.y}
              className="absolute left-2 text-xs text-neutral-500 tabular-nums -translate-y-1/2"
              style={{ top: g.y }}
            >
              {g.value.toFixed(2)}
            </span>
          ))}
        </div>
      </div>
      <div className="flex justify-between text-xs text-neutral-600 mt-2">
        <span>{first.date}</span>
        <span>{last.date}</span>
      </div>
    </div>
  );
}
