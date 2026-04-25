export interface VeiculoResponse {
  id: number
  placa: string
  marca: string
  ano: number
  cor: string
  precoUsd: number
  precoBrlEstimado: number
  ativo: boolean
  createdAt: string
  updatedAt: string
}

export interface VeiculoCreateRequest {
  placa: string
  marca: string
  ano: number
  cor: string
  precoUsd: number
}

export type VeiculoPutRequest = VeiculoCreateRequest

export interface VeiculoPatchRequest {
  placa?: string
  marca?: string
  ano?: number
  cor?: string
  precoUsd?: number
}

export interface VeiculoFiltros {
  marca?: string
  ano?: number
  cor?: string
  minPreco?: number
  maxPreco?: number
}

export interface RelatorioMarcaResponse {
  marca: string
  quantidade: number
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
