const unifiedUrl = import.meta.env.VITE_PULSESTACK_URL;

export const AUTH_BASE_URL =
  unifiedUrl ?? import.meta.env.VITE_AUTH_URL ?? 'http://localhost:8080';

export const INGESTION_BASE_URL =
  unifiedUrl ?? import.meta.env.VITE_INGESTION_URL ?? 'http://localhost:8080';

export const PROCESSING_BASE_URL =
  unifiedUrl ?? import.meta.env.VITE_PROCESSING_URL ?? 'http://localhost:8080';

export const CHAT_BASE_URL =
  unifiedUrl ?? import.meta.env.VITE_CHAT_URL ?? 'http://localhost:8080';
