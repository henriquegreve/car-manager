import type {
  Page,
  VeiculoCreateRequest,
  VeiculoFiltros,
  VeiculoPatchRequest,
  VeiculoResponse,
} from '@/types/veiculo'
import { client } from './client'

export interface ListParams extends VeiculoFiltros {
  page?: number
  size?: number
  sort?: string
}

export async function listarVeiculos(params: ListParams = {}): Promise<Page<VeiculoResponse>> {
  const res = await client.get<Page<VeiculoResponse>>('/veiculos', { params })
  return res.data
}

export async function buscarVeiculo(id: number): Promise<VeiculoResponse> {
  const res = await client.get<VeiculoResponse>(`/veiculos/${id}`)
  return res.data
}

export async function criarVeiculo(data: VeiculoCreateRequest): Promise<VeiculoResponse> {
  const res = await client.post<VeiculoResponse>('/veiculos', data)
  return res.data
}

export async function editarVeiculo(
  id: number,
  data: VeiculoCreateRequest,
): Promise<VeiculoResponse> {
  const res = await client.put<VeiculoResponse>(`/veiculos/${id}`, data)
  return res.data
}

export async function patchVeiculo(
  id: number,
  data: VeiculoPatchRequest,
): Promise<VeiculoResponse> {
  const res = await client.patch<VeiculoResponse>(`/veiculos/${id}`, data)
  return res.data
}

export async function removerVeiculo(id: number): Promise<void> {
  await client.delete(`/veiculos/${id}`)
}
