# Car Manager Web

Front do monorepo em React + Vite + TypeScript. Fluxo principal: login com JWT, listagem e cadastro de veículos, detalhe com preço estimado em BRL e tela de relatório por marca (inclui download de PDF pela API).

Instruções para subir API, banco e Redis estão no [README da raiz](../README.md).

## Rodar em desenvolvimento

Precisa da API em **http://localhost:8080** (o Vite repassa `/api` pra lá — ver `vite.config.ts`).

```bash
npm install
npm run dev
```

Build de produção: `npm run build`.
