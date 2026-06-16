import { useState } from 'react';
import { SimpleLineChart } from './SimpleLineChart';
import { usePredictor } from '../hooks/usePredictor';
import type { Direction } from '../types/Predictor';

type Tab = 'spielen' | 'dashboard' | 'rangliste';

interface PredictorViewProps {
  token: string | null;
}

export function PredictorView({ token }: PredictorViewProps) {
  const [tab, setTab] = useState<Tab>('spielen');
  const {
    dailyStock, myPredictions, leaderboard, resolvedChallenge,
    loading, error, submitPrediction,
  } = usePredictor(token);

  return (
    <div className="flex-1 flex flex-col overflow-hidden bg-gray-950 text-white">
      {/* Obere Hälfte — Tabs */}
      <div className="flex-1 min-h-0 overflow-y-auto border-b border-gray-800">
        <div className="flex items-center gap-1 px-4 pt-4">
          {(['spielen', 'dashboard', 'rangliste'] as Tab[]).map(t => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`px-4 py-2 rounded-lg text-sm font-medium capitalize transition-colors ${
                tab === t ? 'bg-indigo-600 text-white' : 'text-gray-400 hover:text-white'
              }`}
            >
              {t}
            </button>
          ))}
        </div>

        <div className="p-4">
          {loading && <p className="text-gray-400">Lade…</p>}
          {error && <p className="text-red-400">{error}</p>}

          {!loading && tab === 'spielen' && (
            <SpielenTab dailyStock={dailyStock} onSubmit={submitPrediction} />
          )}
          {!loading && tab === 'dashboard' && (
            <DashboardTab predictions={myPredictions} />
          )}
          {!loading && tab === 'rangliste' && (
            <RanglisteTab entries={leaderboard} />
          )}
        </div>
      </div>

      {/* Untere Hälfte — Auflösung der Aktie */}
      <div className="flex-1 min-h-0 overflow-y-auto p-4">
        <h3 className="text-sm font-semibold text-gray-400 uppercase tracking-wide mb-3">
          Letzte Auflösung
        </h3>
        {!resolvedChallenge && (
          <p className="text-gray-500 text-sm">Noch keine aufgelöste Challenge vorhanden.</p>
        )}
        {resolvedChallenge && (
          <ResolutionPanel challenge={resolvedChallenge} />
        )}
      </div>
    </div>
  );
}

function SpielenTab({
  dailyStock,
  onSubmit,
}: {
  dailyStock: ReturnType<typeof usePredictor>['dailyStock'];
  onSubmit: (stockId: string, direction: Direction, predictedPrice: number) => Promise<void>;
}) {
  const [direction, setDirection] = useState<Direction | null>(null);
  const [targetPrice, setTargetPrice] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');
  const [submitted, setSubmitted] = useState(false);

  if (!dailyStock) {
    return <p className="text-gray-500 text-sm">Heutige Challenge noch nicht verfügbar.</p>;
  }

  const lastPrice = dailyStock.prices[dailyStock.prices.length - 1]?.close ?? 0;
  const chartData = dailyStock.prices.map(p => ({ date: p.date, close: p.close }));

  const handleSubmit = async () => {
    if (!direction || !targetPrice) return;
    setSubmitting(true);
    setSubmitError('');
    try {
      await onSubmit(dailyStock.stockId, direction, Number(targetPrice));
      setSubmitted(true);
    } catch (e) {
      setSubmitError(e instanceof Error ? e.message : 'Fehler beim Absenden.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <div>
          <p className="text-xs text-gray-500 uppercase tracking-wide">Unbekannte Aktie</p>
          <p className="text-lg font-semibold">{dailyStock.codename}</p>
        </div>
        <div className="text-right">
          <p className="text-xs text-gray-500 uppercase tracking-wide">Letzter Kurs</p>
          <p className="text-lg font-semibold">${lastPrice.toFixed(2)}</p>
        </div>
      </div>

      <div className="bg-gray-900 rounded-xl p-3 mb-4">
        <SimpleLineChart data={chartData} height={260} color="#818cf8" />
      </div>

      <p className="text-xs text-gray-500 mb-3">Zieldatum: {dailyStock.targetDate}</p>

      {submitted ? (
        <p className="text-green-400 text-sm">Vorhersage abgegeben! Schau im Dashboard-Tab nach.</p>
      ) : (
        <>
          <div className="grid grid-cols-2 gap-3 mb-3">
            <button
              onClick={() => setDirection('UP')}
              className={`py-3 rounded-lg text-sm font-medium border transition-colors ${
                direction === 'UP'
                  ? 'bg-green-600/20 border-green-500 text-green-400'
                  : 'border-gray-700 text-gray-400 hover:border-gray-600'
              }`}
            >
              ↑ Steigt
            </button>
            <button
              onClick={() => setDirection('DOWN')}
              className={`py-3 rounded-lg text-sm font-medium border transition-colors ${
                direction === 'DOWN'
                  ? 'bg-red-600/20 border-red-500 text-red-400'
                  : 'border-gray-700 text-gray-400 hover:border-gray-600'
              }`}
            >
              ↓ Fällt
            </button>
          </div>

          <label className="block text-xs text-gray-500 uppercase tracking-wide mb-1">Zielpreis ($)</label>
          <input
            type="number"
            step="0.01"
            value={targetPrice}
            onChange={e => setTargetPrice(e.target.value)}
            placeholder={lastPrice.toFixed(2)}
            className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-sm mb-3 focus:outline-none focus:border-indigo-500"
          />

          {submitError && <p className="text-red-400 text-xs mb-3">{submitError}</p>}

          <button
            onClick={handleSubmit}
            disabled={!direction || !targetPrice || submitting}
            className="w-full bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-800 disabled:text-gray-500 rounded-lg py-2.5 text-sm font-medium transition-colors"
          >
            {submitting ? 'Wird gesendet…' : 'Vorhersage abgeben'}
          </button>
        </>
      )}
    </div>
  );
}

function DashboardTab({ predictions }: { predictions: ReturnType<typeof usePredictor>['myPredictions'] }) {
  if (predictions.length === 0) {
    return <p className="text-gray-500 text-sm">Du hast noch keine Vorhersagen abgegeben.</p>;
  }

  return (
    <div className="space-y-2">
      {predictions.map(p => (
        <div key={p.id} className="bg-gray-900 rounded-lg p-3 flex items-center justify-between">
          <div>
            <p className="text-sm font-medium">{p.stockCodename}</p>
            <p className="text-xs text-gray-500">Ziel: {p.targetDate}</p>
          </div>
          <div className="text-right">
            <p className={`text-sm font-medium ${p.direction === 'UP' ? 'text-green-400' : 'text-red-400'}`}>
              {p.direction === 'UP' ? '↑' : '↓'} ${p.predictedPrice.toFixed(2)}
            </p>
            <p className={`text-xs ${p.status === 'RESOLVED' ? 'text-gray-400' : 'text-yellow-400'}`}>
              {p.status === 'RESOLVED' ? 'Aufgelöst' : 'Ausstehend'}
            </p>
          </div>
        </div>
      ))}
    </div>
  );
}

function RanglisteTab({ entries }: { entries: ReturnType<typeof usePredictor>['leaderboard'] }) {
  if (entries.length === 0) {
    return <p className="text-gray-500 text-sm">Noch keine Rangliste verfügbar.</p>;
  }

  return (
    <table className="w-full text-sm">
      <thead>
        <tr className="text-left text-gray-500 text-xs uppercase tracking-wide">
          <th className="pb-2 pr-3">#</th>
          <th className="pb-2 pr-3">Name</th>
          <th className="pb-2 pr-3 text-right">Score</th>
          <th className="pb-2 text-right">Aufgelöst</th>
        </tr>
      </thead>
      <tbody>
        {entries.map(e => (
          <tr key={e.rank} className="border-t border-gray-800">
            <td className="py-2 pr-3 text-gray-400">{e.rank}</td>
            <td className="py-2 pr-3">{e.username}</td>
            <td className="py-2 pr-3 text-right font-medium">{e.totalScore}</td>
            <td className="py-2 text-right text-gray-400">{e.predictionsResolved}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function ResolutionPanel({
  challenge,
}: {
  challenge: NonNullable<ReturnType<typeof usePredictor>['resolvedChallenge']>;
}) {
  const chartData = challenge.prices.map(p => ({ date: p.date, close: p.close }));

  return (
    <div>
      <div className="flex items-center justify-between mb-3">
        <div>
          <p className="text-lg font-semibold">
            {challenge.ticker} — {challenge.companyName}
          </p>
          <p className="text-xs text-gray-500">Challenge vom {challenge.challengeDate}</p>
        </div>
        {challenge.currentPrice !== null && (
          <div className="text-right">
            <p className="text-xs text-gray-500 uppercase tracking-wide">Aktueller Kurs</p>
            <p className="text-lg font-semibold">${challenge.currentPrice.toFixed(2)}</p>
          </div>
        )}
      </div>

      {chartData.length > 0 && (
        <div className="bg-gray-900 rounded-xl p-3">
          <SimpleLineChart data={chartData} height={220} color="#34d399" />
        </div>
      )}
    </div>
  );
}
