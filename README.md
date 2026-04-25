# Car Manager

Sistema WEB para gerenciamento e listagem de veículos.

## O que tem aqui

- **car-manager-api** — API REST com autenticação JWT, criada utilizando Java 21, Spring Boot, integração com AwesomeAPI e Frankfurter em caso de fallback, JasperReports. Conta com autenticação de usuário, onde usuário comum (USER) com permissão apenas de leitura e usuário admin (ADMIN) realiza o CRUD, soft delete, validação de duplicação de registros e geração de relatório por marca de veículo.

- **car-manager-web** — Aplicação WEB criada utilizando React (com TypeScript), empacotado com Vite, React Router, UI com shadcn e Tailwind CSS. Possui tela de login, listagem de veículos, formulário para criação de novos veículos e tela para geração de relatório por marca.

O projeto conta com um docker-compose para iniciar os serviços necessários referente a Postgres, Redis, API (car-management-api) e build estático (car-manager-web) na porta 3000.

## Como iniciar

## Docker

Na pasta do repositório:

```bash
docker compose up --build
```

Após inicialização, o car-manager-web é iniciado no caminho **http://localhost:3000** e car-manager-api no caminho **http://localhost:8080**. 

Os usuários de teste são: 
Username: user e Password: user para usuário "comum" com permissão apenas de leitura. 
Username: admin e Password: admin para usuário com permissão "completa" (realizando o CRUD completo).

O Token de autenticação é gerado através de um POST em `/auth/login` na API (car-manager-api).

## Swagger e PDF

- Swagger UI: **http://localhost:8080/swagger-ui.html** — autorize com Bearer usando o token do `/auth/login`.
- PDF: autenticado, **GET /veiculos/relatorios/por-marca/pdf** (o front usa isso no botão “Gerar PDF” na tela de relatório).

## Obs

Se algo não subir (porta 5432 ocupada, Redis em outro host, etc.), vale conferir o `docker-compose.yml` e o `application.properties` da API e alinhar com o seu ambiente.
