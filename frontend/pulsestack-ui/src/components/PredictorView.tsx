import { useState } from 'react';
import { SimpleLineChart } from './SimpleLineChart';
import { usePredictor } from '../hooks/usePredictor';
import type { Direction } from '../types/Predictor';

type Tab = 'spielen' | 'dashboard' | 'rangliste';

interface PredictorViewProps {
  token: string | null;
  username: string | null;
}

export function PredictorView({ token, username }: PredictorViewProps) {
  const [tab, setTab] = useState<Tab>('spielen');
  const {
    dailyStock, myPredictions, leaderboard, resolvedChallenge,
    loading, error, submitPrediction,
  } = usePredictor(token);

  return (
    <div className="flex-1 flex flex-col overflow-hidden bg-[#0a0a0a] text-white relative">
      {/* Background grid — wie im Original */}
      <div
        className="fixed inset-0 opacity-[0.03] pointer-events-none"
        style={{
          backgroundImage:
            'linear-gradient(#fff 1px, transparent 1px), linear-gradient(90deg, #fff 1px, transparent 1px)',
          backgroundSize: '60px 60px',
        }}
      />

      {/* Obere Hälfte — Tabs */}
      <div className="flex-1 min-h-0 overflow-y-auto border-b border-neutral-900 relative z-10">
        <div className="flex items-center gap-8 px-6 py-5 border-b border-neutral-900">
          {(['spielen', 'dashboard', 'rangliste'] as Tab[]).map(t => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`text-xs tracking-widest uppercase transition-colors ${
                tab === t ? 'text-white' : 'text-neutral-500 hover:text-white'
              }`}
            >
              {t}
            </button>
          ))}
        </div>

        <div className="px-6 py-8 max-w-4xl">
          {loading && (
            <p className="text-xs text-neutral-500 tracking-widest uppercase">Lade…</p>
          )}
          {error && <p className="text-red-400 text-sm">{error}</p>}

          {!loading && tab === 'spielen' && (
            <SpielenTab dailyStock={dailyStock} onSubmit={submitPrediction} />
          )}
          {!loading && tab === 'dashboard' && (
            <DashboardTab predictions={myPredictions} username={username} />
          )}
          {!loading && tab === 'rangliste' && (
            <RanglisteTab entries={leaderboard} currentUser={username} />
          )}
        </div>
      </div>

      {/* Untere Hälfte — Auflösung der Aktie */}
      <div className="flex-1 min-h-0 overflow-y-auto px-6 py-8 relative z-10">
        <p className="text-xs text-neutral-500 tracking-widest uppercase mb-4">
          Letzte Auflösung
        </p>
        {!resolvedChallenge && (
          <p className="text-neutral-600 text-sm">Noch keine aufgelöste Challenge vorhanden.</p>
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
    return <p className="text-neutral-600 text-sm">Heutige Challenge noch nicht verfügbar.</p>;
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

  if (submitted) {
    return (
      <div className="space-y-8 pt-4">
        <div className="w-px h-16 bg-neutral-700 mx-auto" />
        <div className="text-center">
          <p className="text-xs text-neutral-500 tracking-widest uppercase mb-3">
            Vorhersage gespeichert
          </p>
          <h1 className="text-3xl font-light tracking-tight mb-1">
            {direction === 'UP' ? '↑ Steigt' : '↓ Fällt'}
          </h1>
          <p className="text-4xl font-light tabular-nums mt-4">${Number(targetPrice).toFixed(2)}</p>
        </div>
        <p className="text-xs text-neutral-600 text-center">
          Das Ergebnis wird am {dailyStock.targetDate} aufgedeckt.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-10">
      {/* Stock header */}
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs text-neutral-500 tracking-widest uppercase mb-1">Unbekannte Aktie</p>
          <h1 className="text-2xl font-light tracking-tight">{dailyStock.codename}</h1>
        </div>
        <div className="text-right">
          <p className="text-xs text-neutral-500 tracking-widest uppercase mb-1">Letzter Kurs</p>
          <p className="text-2xl font-light tabular-nums">${lastPrice.toFixed(2)}</p>
        </div>
      </div>

      {/* Chart */}
      <div className="border border-neutral-900 bg-neutral-950 p-4">
        <SimpleLineChart data={chartData} height={260} />
      </div>

      {/* Info bar */}
      <div className="flex items-center gap-8 text-xs text-neutral-500 border-t border-neutral-900 pt-4">
        <div>
          <span className="tracking-widest uppercase">Zeitraum</span>
          <span className="ml-3 text-neutral-400">
            {dailyStock.prices[0]?.date} → {dailyStock.prices[dailyStock.prices.length - 1]?.date}
          </span>
        </div>
        <div>
          <span className="tracking-widest uppercase">Zieldatum</span>
          <span className="ml-3 text-neutral-400">{dailyStock.targetDate}</span>
        </div>
        <div>
          <span className="tracking-widest uppercase">Datenpunkte</span>
          <span className="ml-3 text-neutral-400">{dailyStock.prices.length}</span>
        </div>
      </div>

      {/* Prediction form */}
      <div className="border border-neutral-900 p-6 space-y-6">
        <p className="text-xs text-neutral-500 tracking-widest uppercase">
          Deine Vorhersage für {dailyStock.targetDate}
        </p>

        <div className="grid grid-cols-2 gap-3">
          <button
            onClick={() => setDirection('UP')}
            className={`py-4 text-xs tracking-widest uppercase border transition-all ${
              direction === 'UP'
                ? 'border-green-500 text-green-400 bg-green-500/5'
                : 'border-neutral-800 text-neutral-500 hover:border-neutral-600'
            }`}
          >
            ↑ Steigt
          </button>
          <button
            onClick={() => setDirection('DOWN')}
            className={`py-4 text-xs tracking-widest uppercase border transition-all ${
              direction === 'DOWN'
                ? 'border-red-500 text-red-400 bg-red-500/5'
                : 'border-neutral-800 text-neutral-500 hover:border-neutral-600'
            }`}
          >
            ↓ Fällt
          </button>
        </div>

        <div>
          <label className="block text-xs text-neutral-500 tracking-widest uppercase mb-2">Zielpreis ($)</label>
          <div className="flex items-center border border-neutral-800 focus-within:border-neutral-500 transition-colors">
            <span className="px-4 text-neutral-600 text-sm">$</span>
            <input
              type="number"
              step="0.01"
              min="0.01"
              value={targetPrice}
              onChange={e => setTargetPrice(e.target.value)}
              placeholder={lastPrice.toFixed(2)}
              className="flex-1 bg-transparent text-white px-2 py-3 text-sm outline-none placeholder:text-neutral-700"
            />
          </div>
        </div>

        {submitError && <p className="text-red-400 text-xs tracking-wide">{submitError}</p>}

        <button
          onClick={handleSubmit}
          disabled={!direction || !targetPrice || submitting}
          className="w-full bg-white text-black text-xs tracking-widest uppercase py-3 hover:bg-neutral-200 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
        >
          {submitting ? 'Wird gespeichert…' : 'Vorhersage abgeben'}
        </button>
      </div>
    </div>
  );
}

function DashboardTab({
  predictions,
  username,
}: {
  predictions: ReturnType<typeof usePredictor>['myPredictions'];
  username: string | null;
}) {
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState<'ALL' | 'PENDING' | 'RESOLVED'>('ALL');

  const filtered = predictions.filter(p => {
    const matchesFilter = filter === 'ALL' || p.status === filter;
    const matchesSearch =
      search === '' ||
      p.stockCodename.toLowerCase().includes(search.toLowerCase()) ||
      (p.result?.ticker ?? '').toLowerCase().includes(search.toLowerCase());
    return matchesFilter && matchesSearch;
  });

  const totalScore = predictions.filter(p => p.result).reduce((sum, p) => sum + (p.result?.totalScore ?? 0), 0);
  const resolved = predictions.filter(p => p.status === 'RESOLVED').length;
  const pending = predictions.filter(p => p.status === 'PENDING').length;

  return (
    <div>
      {/* Header */}
      <div className="mb-12">
        <p className="text-xs text-neutral-500 tracking-widest uppercase mb-2">Dashboard</p>
        <h1 className="text-3xl font-light tracking-tight">{username}</h1>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-4 mb-12">
        <div className="border border-neutral-900 p-5">
          <p className="text-3xl font-light tabular-nums">{totalScore}</p>
          <p className="text-xs text-neutral-500 tracking-widest uppercase mt-2">Gesamtpunkte</p>
        </div>
        <div className="border border-neutral-900 p-5">
          <p className="text-3xl font-light tabular-nums">{resolved}</p>
          <p className="text-xs text-neutral-500 tracking-widest uppercase mt-2">Abgeschlossen</p>
        </div>
        <div className="border border-neutral-900 p-5">
          <p className="text-3xl font-light tabular-nums">{pending}</p>
          <p className="text-xs text-neutral-500 tracking-widest uppercase mt-2">Ausstehend</p>
        </div>
      </div>

      {/* Suche + Filter */}
      <div className="flex items-center gap-3 mb-6">
        <input
          type="text"
          placeholder="Aktie suchen…"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="flex-1 bg-transparent border border-neutral-800 text-white px-4 py-2 text-sm outline-none focus:border-neutral-500 transition-colors placeholder:text-neutral-700"
        />
        {(['ALL', 'PENDING', 'RESOLVED'] as const).map(f => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className={`text-xs tracking-widest uppercase px-4 py-2 border transition-colors ${
              filter === f ? 'border-white text-white' : 'border-neutral-800 text-neutral-500 hover:border-neutral-600'
            }`}
          >
            {f === 'ALL' ? 'Alle' : f === 'PENDING' ? 'Ausstehend' : 'Abgeschlossen'}
          </button>
        ))}
      </div>

      {/* Tabelle */}
      {filtered.length === 0 ? (
        <div className="border border-neutral-900 p-12 text-center">
          <p className="text-neutral-500 text-sm">
            {predictions.length === 0 ? 'Noch keine Vorhersagen. Starte jetzt!' : 'Keine Ergebnisse gefunden.'}
          </p>
        </div>
      ) : (
        <div className="border border-neutral-900 divide-y divide-neutral-900">
          <div className="grid grid-cols-5 px-4 py-3 text-xs text-neutral-600 tracking-widest uppercase">
            <span className="col-span-2">Aktie</span>
            <span>Richtung</span>
            <span>Zieldatum</span>
            <span className="text-right">Score</span>
          </div>
          {filtered.map(p => (
            <div key={p.id} className="grid grid-cols-5 px-4 py-4 text-sm items-center">
              <div className="col-span-2">
                <p className="text-white text-xs">{p.result ? p.result.ticker : p.stockCodename}</p>
                <p className="text-neutral-600 text-xs mt-0.5">
                  {p.result ? p.result.companyName : `Wird aufgedeckt am ${p.targetDate}`}
                </p>
              </div>
              <div>
                <span className={`text-xs tracking-widest ${p.direction === 'UP' ? 'text-green-400' : 'text-red-400'}`}>
                  {p.direction === 'UP' ? '↑ Steigt' : '↓ Fällt'}
                </span>
                {p.result && (
                  <p className="text-xs mt-0.5">
                    {p.result.directionCorrect
                      ? <span className="text-green-600">✓ Richtig</span>
                      : <span className="text-red-600">✗ Falsch</span>}
                  </p>
                )}
              </div>
              <div>
                <p className="text-neutral-400 text-xs">{p.targetDate}</p>
                <p className={`text-xs mt-0.5 tracking-widest uppercase ${p.status === 'PENDING' ? 'text-yellow-600' : 'text-neutral-600'}`}>
                  {p.status === 'PENDING' ? 'Ausstehend' : 'Abgeschlossen'}
                </p>
              </div>
              <div className="text-right">
                {p.result ? (
                  <div>
                    <p className="text-white font-light tabular-nums">
                      {p.result.totalScore}<span className="text-neutral-600 text-xs"> / 150</span>
                    </p>
                    <p className="text-neutral-600 text-xs mt-0.5">
                      {p.result.directionScore}+{p.result.accuracyScore}
                    </p>
                  </div>
                ) : (
                  <span className="text-neutral-700 text-xs">—</span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      <p className="text-xs text-neutral-600 mt-8 pt-8 border-t border-neutral-900">
        {predictions.length} Vorhersage{predictions.length !== 1 ? 'n' : ''} gesamt
      </p>
    </div>
  );
}

function RanglisteTab({
  entries,
  currentUser,
}: {
  entries: ReturnType<typeof usePredictor>['leaderboard'];
  currentUser: string | null;
}) {
  const [search, setSearch] = useState('');

  if (entries.length === 0) {
    return (
      <div className="border border-neutral-900 p-12 text-center">
        <p className="text-neutral-500 text-sm">Noch keine Rangliste verfügbar.</p>
      </div>
    );
  }

  const filtered = entries.filter(e => search === '' || e.username.toLowerCase().includes(search.toLowerCase()));
  const currentUserEntry = entries.find(e => e.username === currentUser);

  return (
    <div>
      <div className="mb-12">
        <p className="text-xs text-neutral-500 tracking-widest uppercase mb-2">Stock Predictor</p>
        <h1 className="text-3xl font-light tracking-tight">Rangliste</h1>
      </div>

      {currentUserEntry && (
        <div className="border border-neutral-700 p-5 mb-8 flex items-center justify-between">
          <div className="flex items-center gap-6">
            <span className="text-xs text-neutral-500 tracking-widest uppercase w-8">#{currentUserEntry.rank}</span>
            <div>
              <p className="text-white text-sm">{currentUserEntry.username}</p>
              <p className="text-neutral-500 text-xs mt-0.5">Dein Rang</p>
            </div>
          </div>
          <div className="flex items-center gap-12 text-right">
            <div>
              <p className="text-white tabular-nums">{currentUserEntry.totalScore}</p>
              <p className="text-xs text-neutral-600 tracking-widest uppercase mt-0.5">Punkte</p>
            </div>
            <div>
              <p className="text-white tabular-nums">{currentUserEntry.avgScore.toFixed(0)}</p>
              <p className="text-xs text-neutral-600 tracking-widest uppercase mt-0.5">Ø Score</p>
            </div>
            <div>
              <p className="text-white tabular-nums">{currentUserEntry.predictionsResolved}</p>
              <p className="text-xs text-neutral-600 tracking-widest uppercase mt-0.5">Spiele</p>
            </div>
          </div>
        </div>
      )}

      <div className="mb-6">
        <input
          type="text"
          placeholder="Spieler suchen…"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="w-full bg-transparent border border-neutral-800 text-white px-4 py-2 text-sm outline-none focus:border-neutral-500 transition-colors placeholder:text-neutral-700"
        />
      </div>

      {/* Podium */}
      {search === '' && filtered.length >= 3 && (
        <div className="grid grid-cols-3 gap-3 mb-8">
          <div className="border border-neutral-900 p-5 text-center mt-4">
            <p className="text-2xl text-neutral-500 font-light mb-3">2</p>
            <p className="text-white text-sm truncate">{filtered[1]?.username}</p>
            <p className="text-neutral-400 tabular-nums text-lg font-light mt-2">{filtered[1]?.totalScore}</p>
            <p className="text-xs text-neutral-600 tracking-widest uppercase mt-1">Punkte</p>
          </div>
          <div className="border border-neutral-700 p-5 text-center bg-neutral-950">
            <p className="text-2xl text-white font-light mb-3">1</p>
            <p className="text-white text-sm truncate">{filtered[0]?.username}</p>
            <p className="text-white tabular-nums text-lg font-light mt-2">{filtered[0]?.totalScore}</p>
            <p className="text-xs text-neutral-600 tracking-widest uppercase mt-1">Punkte</p>
          </div>
          <div className="border border-neutral-900 p-5 text-center mt-8">
            <p className="text-2xl text-neutral-600 font-light mb-3">3</p>
            <p className="text-white text-sm truncate">{filtered[2]?.username}</p>
            <p className="text-neutral-400 tabular-nums text-lg font-light mt-2">{filtered[2]?.totalScore}</p>
            <p className="text-xs text-neutral-600 tracking-widest uppercase mt-1">Punkte</p>
          </div>
        </div>
      )}

      {/* Tabelle */}
      {filtered.length === 0 ? (
        <div className="border border-neutral-900 p-12 text-center">
          <p className="text-neutral-500 text-sm">Keine Spieler gefunden.</p>
        </div>
      ) : (
        <div className="border border-neutral-900 divide-y divide-neutral-900">
          <div className="grid grid-cols-5 px-4 py-3 text-xs text-neutral-600 tracking-widest uppercase">
            <span>#</span>
            <span className="col-span-2">Spieler</span>
            <span className="text-right">Ø Score</span>
            <span className="text-right">Punkte</span>
          </div>
          {filtered.map(entry => {
            const isMe = entry.username === currentUser;
            return (
              <div
                key={entry.rank}
                className={`grid grid-cols-5 px-4 py-4 text-sm items-center transition-colors ${
                  isMe ? 'bg-neutral-950 text-white' : 'text-neutral-300'
                }`}
              >
                <span className={`text-xs tabular-nums ${
                  entry.rank === 1 ? 'text-white'
                    : entry.rank === 2 ? 'text-neutral-400'
                    : entry.rank === 3 ? 'text-neutral-500'
                    : 'text-neutral-700'
                }`}>
                  {entry.rank}
                </span>
                <div className="col-span-2 flex items-center gap-2">
                  <span className="text-sm">{entry.username}</span>
                  {isMe && <span className="text-xs text-neutral-600 tracking-widest uppercase">(Du)</span>}
                </div>
                <span className="text-right text-xs text-neutral-500 tabular-nums">
                  {entry.avgScore.toFixed(0)}<span className="text-neutral-700"> / 150</span>
                </span>
                <span className={`text-right tabular-nums ${isMe ? 'text-white' : ''}`}>{entry.totalScore}</span>
              </div>
            );
          })}
        </div>
      )}

      <p className="text-xs text-neutral-600 mt-8 pt-8 border-t border-neutral-900">
        {entries.length} Spieler gesamt
      </p>
    </div>
  );
}

function ResolutionPanel({
  challenge,
}: {
  challenge: NonNullable<ReturnType<typeof usePredictor>['resolvedChallenge']>;
}) {
  const chartData = challenge.prices.map(p => ({ date: p.date, close: p.close }));

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex items-start justify-between">
        <div>
          <h2 className="text-2xl font-light tracking-tight">
            {challenge.ticker} — {challenge.companyName}
          </h2>
          <p className="text-xs text-neutral-500 tracking-widest uppercase mt-1">
            Challenge vom {challenge.challengeDate}
          </p>
        </div>
        {challenge.currentPrice !== null && (
          <div className="text-right">
            <p className="text-xs text-neutral-500 tracking-widest uppercase mb-1">Aktueller Kurs</p>
            <p className="text-2xl font-light tabular-nums">${challenge.currentPrice.toFixed(2)}</p>
          </div>
        )}
      </div>

      {chartData.length > 0 && (
        <div className="border border-neutral-900 bg-neutral-950 p-4">
          <SimpleLineChart data={chartData} height={220} />
        </div>
      )}
    </div>
  );
}
