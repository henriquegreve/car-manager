import { useAuth } from '@/hooks/useAuth'
import { Navigate, Outlet, useLocation } from 'react-router-dom'

interface Props {
  requiredRole?: 'ADMIN'
}

export function PrivateRoute({ requiredRole }: Props) {
  const { user, isAdmin } = useAuth()
  const location = useLocation()

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (requiredRole === 'ADMIN' && !isAdmin) {
    return <Navigate to="/veiculos" replace />
  }

  return <Outlet />
}
