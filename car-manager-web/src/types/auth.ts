export interface LoginRequest {
  username: string
  password: string
}

export interface TokenResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
}

export interface AuthUser {
  username: string
  role: 'USER' | 'ADMIN'
}
