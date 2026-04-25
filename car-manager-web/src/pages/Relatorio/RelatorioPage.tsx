import { relatorioPorMarca } from '@/api/relatorios'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { useQuery } from '@tanstack/react-query'
import { FileDown } from 'lucide-react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'

const COLORS = [
  '#2563eb', '#16a34a', '#dc2626', '#d97706', '#7c3aed',
  '#0891b2', '#be185d', '#059669', '#ea580c', '#4f46e5',
]

export function RelatorioPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['relatorio-marca'],
    queryFn: relatorioPorMarca,
  })

  const sorted = [...(data ?? [])].sort((a, b) => b.quantidade - a.quantidade)

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Relatório por Marca</h1>
          <p className="text-sm text-muted-foreground">
            Quantidade de veículos ativos agrupados por marca
          </p>
        </div>
        <Button type="button" variant="outline" className="shrink-0 gap-2" disabled title="Em breve">
          <FileDown className="size-4" />
          Gerar PDF
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Veículos por marca</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading && <p className="text-muted-foreground text-sm">Carregando…</p>}
          {!isLoading && sorted.length === 0 && (
            <p className="text-muted-foreground text-sm">Nenhum dado disponível.</p>
          )}
          {!isLoading && sorted.length > 0 && (
            <ResponsiveContainer width="100%" height={360}>
              <BarChart data={sorted} margin={{ top: 8, right: 16, left: 0, bottom: 40 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis
                  dataKey="marca"
                  tick={{ fontSize: 12 }}
                  angle={-30}
                  textAnchor="end"
                  interval={0}
                />
                <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                <Tooltip
                  formatter={(value) => [value, 'Veículos']}
                  labelFormatter={(label) => `Marca: ${label}`}
                />
                <Bar dataKey="quantidade" radius={[4, 4, 0, 0]}>
                  {sorted.map((_, index) => (
                    <Cell key={index} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>

      {!isLoading && sorted.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Tabela resumo</CardTitle>
          </CardHeader>
          <CardContent>
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b">
                  <th className="py-2 text-left font-medium text-muted-foreground">Marca</th>
                  <th className="py-2 text-right font-medium text-muted-foreground">Quantidade</th>
                </tr>
              </thead>
              <tbody>
                {sorted.map((item) => (
                  <tr key={item.marca} className="border-b last:border-0">
                    <td className="py-2">{item.marca}</td>
                    <td className="py-2 text-right font-medium">{item.quantidade}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
