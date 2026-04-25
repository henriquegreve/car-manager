import type { LoginRequest, TokenResponse } from '@/types/auth'
import { client } from './client'

export async function login(data: LoginRequest): Promise<TokenResponse> {
  const res = await client.post<TokenResponse>('/auth/login', data)
  return res.data
}
