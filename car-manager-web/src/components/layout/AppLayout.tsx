import { useAuth } from '@/hooks/useAuth'
import { BarChart3, Car, LogOut } from 'lucide-react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { Button } from '../ui/button'
import { Separator } from '../ui/separator'

const navItems = [
  { to: '/veiculos', label: 'Veículos', icon: Car },
  { to: '/relatorios/por-marca', label: 'Relatório por Marca', icon: BarChart3 },
]

export function AppLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <div className="flex min-h-screen">
      <aside className="flex w-60 flex-col border-r bg-sidebar text-sidebar-foreground">
        <div className="flex h-14 items-center px-5">
          <span className="text-base font-semibold tracking-tight">Car Manager</span>
        </div>
        <Separator />
        <nav className="flex flex-1 flex-col gap-1 p-3">
          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-md px-3 py-2 text-sm transition-colors ${
                  isActive
                    ? 'bg-sidebar-primary text-sidebar-primary-foreground font-medium'
                    : 'hover:bg-sidebar-accent hover:text-sidebar-accent-foreground'
                }`
              }
            >
              <Icon size={16} />
              {label}
            </NavLink>
          ))}
        </nav>
        <Separator />
        <div className="flex items-center justify-between p-4">
          <div className="flex flex-col">
            <span className="text-sm font-medium">{user?.username}</span>
            <span className="text-xs text-muted-foreground">{user?.role}</span>
          </div>
          <Button variant="ghost" size="icon" onClick={handleLogout} title="Sair">
            <LogOut size={16} />
          </Button>
        </div>
      </aside>

      <div className="flex flex-1 flex-col">
        <main className="flex-1 p-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
