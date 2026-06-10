export interface AuthResponse {
  token: string;
  username: string;
}

export interface AuthState {
  token: string | null;
  username: string | null;
}
