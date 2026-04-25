import { listarVeiculos, removerVeiculo } from '@/api/veiculos'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog'
import { Badge } from '@/components/ui/badge'
import { Button, buttonVariants } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { useAuth } from '@/hooks/useAuth'
import type { VeiculoFiltros } from '@/types/veiculo'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Eye, Pencil, Plus, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'

const PAGE_SIZE = 10

export function VeiculosPage() {
  const { isAdmin } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [page, setPage] = useState(0)
  const [filtros, setFiltros] = useState<VeiculoFiltros>({})
  const [draft, setDraft] = useState<VeiculoFiltros>({})

  const { data, isFetching } = useQuery({
    queryKey: ['veiculos', page, filtros],
    queryFn: () => listarVeiculos({ ...filtros, page, size: PAGE_SIZE }),
  })

  const deleteMutation = useMutation({
    mutationFn: removerVeiculo,
    onSuccess: () => {
      toast.success('Veículo removido.')
      queryClient.invalidateQueries({ queryKey: ['veiculos'] })
    },
    onError: () => toast.error('Erro ao remover veículo.'),
  })

  function applyFilters() {
    setFiltros({ ...draft })
    setPage(0)
  }

  function clearFilters() {
    setDraft({})
    setFiltros({})
    setPage(0)
  }

  const veiculos = data?.content ?? []
  const totalPages = data?.totalPages ?? 1

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Veículos</h1>
          <p className="text-sm text-muted-foreground">
            {data ? `${data.totalElements} registro(s) encontrado(s)` : '…'}
          </p>
        </div>
        {isAdmin && (
          <Link to="/veiculos/novo" className={buttonVariants()}>
            <Plus size={16} />
            Novo Veículo
          </Link>
        )}
      </div>

      {/* Filtros */}
      <div className="grid grid-cols-2 gap-3 rounded-lg border p-4 md:grid-cols-3 lg:grid-cols-5">
        <div className="flex flex-col gap-1">
          <Label className="text-xs">Marca</Label>
          <Input
            placeholder="Ex: Toyota"
            value={draft.marca ?? ''}
            onChange={(e) => setDraft((p) => ({ ...p, marca: e.target.value || undefined }))}
          />
        </div>
        <div className="flex flex-col gap-1">
          <Label className="text-xs">Ano</Label>
          <Input
            type="number"
            placeholder="Ex: 2022"
            value={draft.ano ?? ''}
            onChange={(e) =>
              setDraft((p) => ({ ...p, ano: e.target.value ? Number(e.target.value) : undefined }))
            }
          />
        </div>
        <div className="flex flex-col gap-1">
          <Label className="text-xs">Cor</Label>
          <Input
            placeholder="Ex: Prata"
            value={draft.cor ?? ''}
            onChange={(e) => setDraft((p) => ({ ...p, cor: e.target.value || undefined }))}
          />
        </div>
        <div className="flex flex-col gap-1">
          <Label className="text-xs">Preço mín. (USD)</Label>
          <Input
            type="number"
            placeholder="0"
            value={draft.minPreco ?? ''}
            onChange={(e) =>
              setDraft((p) => ({
                ...p,
                minPreco: e.target.value ? Number(e.target.value) : undefined,
              }))
            }
          />
        </div>
        <div className="flex flex-col gap-1">
          <Label className="text-xs">Preço máx. (USD)</Label>
          <Input
            type="number"
            placeholder="999999"
            value={draft.maxPreco ?? ''}
            onChange={(e) =>
              setDraft((p) => ({
                ...p,
                maxPreco: e.target.value ? Number(e.target.value) : undefined,
              }))
            }
          />
        </div>
        <div className="col-span-full flex gap-2">
          <Button size="sm" onClick={applyFilters}>
            Filtrar
          </Button>
          <Button size="sm" variant="outline" onClick={clearFilters}>
            Limpar
          </Button>
        </div>
      </div>

      {/* Tabela */}
      <div className="rounded-lg border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Placa</TableHead>
              <TableHead>Marca</TableHead>
              <TableHead>Ano</TableHead>
              <TableHead>Cor</TableHead>
              <TableHead className="text-right">Preço USD</TableHead>
              <TableHead className="text-right">Preço BRL</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isFetching && veiculos.length === 0 && (
              <TableRow>
                <TableCell colSpan={8} className="py-8 text-center text-muted-foreground">
                  Carregando…
                </TableCell>
              </TableRow>
            )}
            {!isFetching && veiculos.length === 0 && (
              <TableRow>
                <TableCell colSpan={8} className="py-8 text-center text-muted-foreground">
                  Nenhum veículo encontrado.
                </TableCell>
              </TableRow>
            )}
            {veiculos.map((v) => (
              <TableRow key={v.id} className={isFetching ? 'opacity-50' : ''}>
                <TableCell className="font-mono font-medium">{v.placa}</TableCell>
                <TableCell>{v.marca}</TableCell>
                <TableCell>{v.ano}</TableCell>
                <TableCell>{v.cor}</TableCell>
                <TableCell className="text-right">
                  {v.precoUsd.toLocaleString('en-US', { style: 'currency', currency: 'USD' })}
                </TableCell>
                <TableCell className="text-right">
                  {v.precoBrlEstimado.toLocaleString('pt-BR', {
                    style: 'currency',
                    currency: 'BRL',
                  })}
                </TableCell>
                <TableCell>
                  <Badge variant={v.ativo ? 'default' : 'secondary'}>
                    {v.ativo ? 'Ativo' : 'Inativo'}
                  </Badge>
                </TableCell>
                <TableCell>
                  <div className="flex justify-end gap-1">
                    <Button
                      variant="ghost"
                      size="icon"
                      title="Detalhar"
                      onClick={() => navigate(`/veiculos/${v.id}`)}
                    >
                      <Eye size={15} />
                    </Button>
                    {isAdmin && (
                      <>
                        <Button
                          variant="ghost"
                          size="icon"
                          title="Editar"
                          onClick={() => navigate(`/veiculos/${v.id}/editar`)}
                        >
                          <Pencil size={15} />
                        </Button>
                        <AlertDialog>
                          <AlertDialogTrigger
                            render={
                              <Button
                                variant="ghost"
                                size="icon"
                                title="Remover"
                                className="text-destructive hover:text-destructive"
                              />
                            }
                          >
                            <Trash2 size={15} />
                          </AlertDialogTrigger>
                          <AlertDialogContent>
                            <AlertDialogHeader>
                              <AlertDialogTitle>Remover veículo?</AlertDialogTitle>
                              <AlertDialogDescription>
                                O veículo <strong>{v.placa}</strong> será desativado (soft delete).
                                Esta ação pode ser revertida via API.
                              </AlertDialogDescription>
                            </AlertDialogHeader>
                            <AlertDialogFooter>
                              <AlertDialogCancel>Cancelar</AlertDialogCancel>
                              <AlertDialogAction
                                className="bg-destructive text-white hover:bg-destructive/90"
                                onClick={() => deleteMutation.mutate(v.id)}
                              >
                                Remover
                              </AlertDialogAction>
                            </AlertDialogFooter>
                          </AlertDialogContent>
                        </AlertDialog>
                      </>
                    )}
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {/* Paginação */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between text-sm">
          <span className="text-muted-foreground">
            Página {page + 1} de {totalPages}
          </span>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              disabled={page === 0}
              onClick={() => setPage((p) => p - 1)}
            >
              Anterior
            </Button>
            <Button
              variant="outline"
              size="sm"
              disabled={page + 1 >= totalPages}
              onClick={() => setPage((p) => p + 1)}
            >
              Próxima
            </Button>
          </div>
        </div>
      )}
    </div>
  )
}
