import { buscarVeiculo } from '@/api/veiculos'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { useAuth } from '@/hooks/useAuth'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Pencil } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'

function Field({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-0.5">
      <span className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
        {label}
      </span>
      <span className="text-sm font-medium">{value}</span>
    </div>
  )
}

export function VeiculoDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { isAdmin } = useAuth()
  const navigate = useNavigate()

  const { data: veiculo, isLoading } = useQuery({
    queryKey: ['veiculo', id],
    queryFn: () => buscarVeiculo(Number(id)),
    enabled: !!id,
  })

  if (isLoading) {
    return <p className="text-muted-foreground">Carregando…</p>
  }

  if (!veiculo) {
    return <p className="text-destructive">Veículo não encontrado.</p>
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="icon" onClick={() => navigate('/veiculos')}>
            <ArrowLeft size={18} />
          </Button>
          <h1 className="text-2xl font-semibold">
            {veiculo.marca} — {veiculo.placa}
          </h1>
        </div>
        {isAdmin && (
          <Button onClick={() => navigate(`/veiculos/${veiculo.id}/editar`)}>
            <Pencil size={14} />
            Editar
          </Button>
        )}
      </div>

      <Card className="max-w-xl">
        <CardHeader>
          <CardTitle className="flex items-center justify-between text-base">
            Detalhes
            <Badge variant={veiculo.ativo ? 'default' : 'secondary'}>
              {veiculo.ativo ? 'Ativo' : 'Inativo'}
            </Badge>
          </CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          <div className="grid grid-cols-2 gap-4">
            <Field label="Placa" value={<span className="font-mono">{veiculo.placa}</span>} />
            <Field label="Marca" value={veiculo.marca} />
            <Field label="Ano" value={veiculo.ano} />
            <Field label="Cor" value={veiculo.cor} />
          </div>
          <Separator />
          <div className="grid grid-cols-2 gap-4">
            <Field
              label="Preço (USD)"
              value={veiculo.precoUsd.toLocaleString('en-US', {
                style: 'currency',
                currency: 'USD',
              })}
            />
            <Field
              label="Preço estimado (BRL)"
              value={veiculo.precoBrlEstimado.toLocaleString('pt-BR', {
                style: 'currency',
                currency: 'BRL',
              })}
            />
          </div>
          <Separator />
          <div className="grid grid-cols-2 gap-4">
            <Field
              label="Criado em"
              value={new Date(veiculo.createdAt).toLocaleString('pt-BR')}
            />
            <Field
              label="Atualizado em"
              value={new Date(veiculo.updatedAt).toLocaleString('pt-BR')}
            />
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
