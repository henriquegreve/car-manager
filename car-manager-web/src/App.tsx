import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { Toaster } from './components/ui/sonner'
import { AppLayout } from './components/layout/AppLayout'
import { PrivateRoute } from './router/PrivateRoute'
import { LoginPage } from './pages/Login/LoginPage'
import { VeiculosPage } from './pages/Veiculos/VeiculosPage'
import { VeiculoDetailPage } from './pages/VeiculoDetail/VeiculoDetailPage'
import { VeiculoFormPage } from './pages/VeiculoForm/VeiculoFormPage'
import { RelatorioPage } from './pages/Relatorio/RelatorioPage'

const queryClient = new QueryClient({
  defaultOptions: { queries: { staleTime: 30_000 } },
})

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route element={<PrivateRoute />}>
            <Route element={<AppLayout />}>
              <Route index element={<Navigate to="/veiculos" replace />} />
              <Route path="/veiculos" element={<VeiculosPage />} />
              <Route path="/veiculos/:id" element={<VeiculoDetailPage />} />
              <Route path="/relatorios/por-marca" element={<RelatorioPage />} />

              <Route element={<PrivateRoute requiredRole="ADMIN" />}>
                <Route path="/veiculos/novo" element={<VeiculoFormPage />} />
                <Route path="/veiculos/:id/editar" element={<VeiculoFormPage />} />
              </Route>
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/veiculos" replace />} />
        </Routes>
      </BrowserRouter>
      <Toaster richColors position="top-right" />
    </QueryClientProvider>
  )
}
