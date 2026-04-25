import { buscarVeiculo, criarVeiculo, editarVeiculo } from '@/api/veiculos'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft } from 'lucide-react'
import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate, useParams } from 'react-router-dom'
import { toast } from 'sonner'
import { z } from 'zod'

const schema = z.object({
  placa: z
    .string()
    .regex(/^[A-Za-z0-9-]{7}$/, 'Placa deve ter exatamente 7 caracteres alfanuméricos'),
  marca: z.string().min(1, 'Informe a marca').max(120),
  ano: z
    .string()
    .refine((v) => {
      const n = Number(v)
      return !isNaN(n) && Number.isInteger(n) && n >= 1950 && n <= 2035
    }, 'Ano deve ser entre 1950 e 2035'),
  cor: z.string().min(1, 'Informe a cor').max(60),
  precoUsd: z
    .string()
    .refine((v) => {
      const n = Number(v)
      return !isNaN(n) && n > 0
    }, 'Preço deve ser um número positivo'),
})

type FormData = z.infer<typeof schema>

export function VeiculoFormPage() {
  const { id } = useParams<{ id: string }>()
  const isEdit = !!id
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: veiculo } = useQuery({
    queryKey: ['veiculo', id],
    queryFn: () => buscarVeiculo(Number(id)),
    enabled: isEdit,
  })

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  useEffect(() => {
    if (veiculo) {
      reset({
        placa: veiculo.placa,
        marca: veiculo.marca,
        ano: String(veiculo.ano),
        cor: veiculo.cor,
        precoUsd: String(veiculo.precoUsd),
      })
    }
  }, [veiculo, reset])

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      isEdit
        ? editarVeiculo(Number(id), {
            placa: data.placa,
            marca: data.marca,
            ano: Number(data.ano),
            cor: data.cor,
            precoUsd: Number(data.precoUsd),
          })
        : criarVeiculo({
            placa: data.placa,
            marca: data.marca,
            ano: Number(data.ano),
            cor: data.cor,
            precoUsd: Number(data.precoUsd),
          }),
    onSuccess: (saved) => {
      queryClient.invalidateQueries({ queryKey: ['veiculos'] })
      queryClient.invalidateQueries({ queryKey: ['veiculo', id] })
      toast.success(isEdit ? 'Veículo atualizado.' : 'Veículo cadastrado.')
      navigate(`/veiculos/${saved.id}`)
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      const msg = err?.response?.data?.message ?? 'Erro ao salvar veículo.'
      toast.error(msg)
    },
  })

  function onSubmit(data: FormData) {
    mutation.mutate(data)
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center gap-3">
        <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
          <ArrowLeft size={18} />
        </Button>
        <h1 className="text-2xl font-semibold">
          {isEdit ? 'Editar Veículo' : 'Novo Veículo'}
        </h1>
      </div>

      <Card className="max-w-lg">
        <CardHeader>
          <CardTitle className="text-base">Dados do veículo</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="placa">Placa</Label>
              <Input
                id="placa"
                placeholder="ABC1234"
                className="uppercase"
                maxLength={7}
                {...register('placa')}
              />
              {errors.placa && (
                <span className="text-xs text-destructive">{errors.placa.message}</span>
              )}
            </div>

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="marca">Marca</Label>
              <Input id="marca" placeholder="Toyota" {...register('marca')} />
              {errors.marca && (
                <span className="text-xs text-destructive">{errors.marca.message}</span>
              )}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="flex flex-col gap-1.5">
                <Label htmlFor="ano">Ano</Label>
                <Input id="ano" type="number" placeholder="2023" {...register('ano')} />
                {errors.ano && (
                  <span className="text-xs text-destructive">{errors.ano.message}</span>
                )}
              </div>
              <div className="flex flex-col gap-1.5">
                <Label htmlFor="cor">Cor</Label>
                <Input id="cor" placeholder="Prata" {...register('cor')} />
                {errors.cor && (
                  <span className="text-xs text-destructive">{errors.cor.message}</span>
                )}
              </div>
            </div>

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="precoUsd">Preço (USD)</Label>
              <Input
                id="precoUsd"
                type="number"
                step="0.01"
                placeholder="25000.00"
                {...register('precoUsd')}
              />
              <span className="text-xs text-muted-foreground">
                Valor armazenado em dólar. O BRL estimado é calculado automaticamente pela API.
              </span>
              {errors.precoUsd && (
                <span className="text-xs text-destructive">{errors.precoUsd.message}</span>
              )}
            </div>

            <div className="flex gap-3 pt-2">
              <Button type="submit" disabled={mutation.isPending}>
                {mutation.isPending ? 'Salvando…' : 'Salvar'}
              </Button>
              <Button type="button" variant="outline" onClick={() => navigate(-1)}>
                Cancelar
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
