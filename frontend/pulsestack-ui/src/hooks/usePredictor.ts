import { useCallback, useEffect, useState } from 'react';
import type {
  DailyStock, PredictionResponse, LeaderboardEntry, ResolvedChallenge, Direction,
} from '../types/Predictor';

const BASE_URL = `${import.meta.env.VITE_STOCKPREDICTOR_URL ?? 'http://localhost:8080'}/api`;

function authHeaders(token: string | null): HeadersInit {
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export function usePredictor(token: string | null) {
  const [dailyStock, setDailyStock] = useState<DailyStock | null>(null);
  const [myPredictions, setMyPredictions] = useState<PredictionResponse[]>([]);
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
  const [resolvedChallenge, setResolvedChallenge] = useState<ResolvedChallenge | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadDailyStock = useCallback(() => {
    return fetch(`${BASE_URL}/stocks/daily`, { headers: authHeaders(token) })
      .then(r => (r.ok ? r.json() : null))
      .then(setDailyStock)
      .catch(() => setDailyStock(null));
  }, [token]);

  const loadMyPredictions = useCallback(() => {
    return fetch(`${BASE_URL}/predictions/my`, { headers: authHeaders(token) })
      .then(r => (r.ok ? r.json() : []))
      .then((preds: PredictionResponse[]) => {
        setMyPredictions(preds);
        return preds;
      })
      .catch(() => []);
  }, [token]);

  const loadLeaderboard = useCallback(() => {
    return fetch(`${BASE_URL}/leaderboard`)
      .then(r => (r.ok ? r.json() : []))
      .then(setLeaderboard)
      .catch(() => setLeaderboard([]));
  }, []);

  const loadResolvedChallenge = useCallback(() => {
    return fetch(`${BASE_URL}/challenge/resolved-latest`, { headers: authHeaders(token) })
      .then(r => (r.ok && r.status !== 204 ? r.json() : null))
      .then(setResolvedChallenge)
      .catch(() => setResolvedChallenge(null));
  }, [token]);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      await Promise.all([
        loadDailyStock(),
        loadMyPredictions(),
        loadLeaderboard(),
        loadResolvedChallenge(),
      ]);
    } catch {
      setError('Konnte StockPredictor-Daten nicht laden.');
    } finally {
      setLoading(false);
    }
  }, [loadDailyStock, loadMyPredictions, loadLeaderboard, loadResolvedChallenge]);

  useEffect(() => {
    if (token) refresh();
  }, [token, refresh]);

  const submitPrediction = useCallback(async (stockId: string, direction: Direction, predictedPrice: number) => {
    const res = await fetch(`${BASE_URL}/predictions/submit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeaders(token) },
      body: JSON.stringify({ stockId, direction, predictedPrice }),
    });
    if (!res.ok) {
      const body = await res.json().catch(() => ({}));
      throw new Error(body.message ?? 'Vorhersage konnte nicht abgegeben werden.');
    }
    await loadMyPredictions();
  }, [token, loadMyPredictions]);

  return {
    dailyStock, myPredictions, leaderboard, resolvedChallenge,
    loading, error, refresh, submitPrediction,
  };
}
