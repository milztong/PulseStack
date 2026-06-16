export interface PricePoint {
  date: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export interface DailyStock {
  stockId: string;
  codename: string;
  prices: PricePoint[];
  targetDate: string;
}

export type Direction = 'UP' | 'DOWN';
export type PredictionStatus = 'PENDING' | 'RESOLVED';

export interface PredictionResponse {
  id: string;
  stockId: string;
  stockCodename: string;
  predictedPrice: number;
  direction: Direction;
  basePrice: number;
  targetDate: string;
  submittedAt: string;
  status: PredictionStatus;
}

export interface LeaderboardEntry {
  rank: number;
  username: string;
  totalScore: number;
  predictionsResolved: number;
  avgScore: number;
}

export interface ResolvedChallenge {
  challengeDate: string;
  stockId: string;
  ticker: string;
  companyName: string;
  currentPrice: number | null;
  prices: PricePoint[];
}

export interface PredictionWithResult extends PredictionResponse {
  result?: ResultResponse;
}

export interface ResultResponse {
  resultId: string;
  predictionId: string;
  stockId: string;
  ticker: string;
  companyName: string;
  codename: string;
  predictedPrice: number;
  direction: Direction;
  basePrice: number;
  actualPrice: number;
  directionCorrect: boolean;
  accuracyScore: number;
  directionScore: number;
  totalScore: number;
  resolvedAt: string;
}
