import type { AuthUser } from '@/types/auth'
import { useCallback, useEffect, useState } from 'react'

function decodeRole(token: string): 'USER' | 'ADMIN' {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const authorities: string[] = payload.authorities ?? []
    return authorities.includes('ROLE_ADMIN') ? 'ADMIN' : 'USER'
  } catch {
    return 'USER'
  }
}

function decodeUsername(token: string): string {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.sub ?? ''
  } catch {
    return ''
  }
}

export function useAuth() {
  const [user, setUser] = useState<AuthUser | null>(() => {
    const stored = localStorage.getItem('user')
    return stored ? (JSON.parse(stored) as AuthUser) : null
  })

  const saveToken = useCallback((token: string) => {
    const authUser: AuthUser = {
      username: decodeUsername(token),
      role: decodeRole(token),
    }
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify(authUser))
    setUser(authUser)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
  }, [])

  useEffect(() => {
    const stored = localStorage.getItem('user')
    if (stored) setUser(JSON.parse(stored) as AuthUser)
  }, [])

  const isAdmin = user?.role === 'ADMIN'

  return { user, isAdmin, saveToken, logout }
}
