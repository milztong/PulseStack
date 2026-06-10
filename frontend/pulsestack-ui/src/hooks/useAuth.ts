import { useState, useCallback } from 'react';
import type { AuthState } from '../types/Auth';

const AUTH_URL = 'http://localhost:8084/api/v1/auth';
const STORAGE_KEY = 'pulsestack_auth';

function loadFromStorage(): AuthState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return JSON.parse(raw);
  } catch {
    // ignore
  }
  return { token: null, username: null };
}

export function useAuth() {
  const [auth, setAuth] = useState<AuthState>(loadFromStorage);

  const persist = useCallback((state: AuthState) => {
    setAuth(state);
    if (state.token) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const res = await fetch(`${AUTH_URL}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    });
    if (!res.ok) throw new Error('Invalid credentials');
    const data = await res.json();
    persist({ token: data.token, username: data.username });
  }, [persist]);

  const register = useCallback(async (username: string, email: string, password: string) => {
    const res = await fetch(`${AUTH_URL}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password }),
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message ?? 'Registration failed');
    }
    const data = await res.json();
    persist({ token: data.token, username: data.username });
  }, [persist]);

  const logout = useCallback(() => persist({ token: null, username: null }), [persist]);

  return { auth, login, register, logout };
}
