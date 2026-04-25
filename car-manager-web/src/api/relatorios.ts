import type { RelatorioMarcaResponse } from '@/types/veiculo'
import { client } from './client'

export async function relatorioPorMarca(): Promise<RelatorioMarcaResponse[]> {
  const res = await client.get<RelatorioMarcaResponse[]>('/veiculos/relatorios/por-marca')
  return res.data
}
