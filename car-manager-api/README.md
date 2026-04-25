# Car Manager API

Backend do monorepo **car-manager**. Instruções gerais, Docker e visão do desafio estão no [README da raiz](../README.md).

## Rodar só a API

Com Postgres e Redis no ar (veja o compose na raiz):

```bash
./mvnw spring-boot:run
```

No Windows use `mvnw.cmd spring-boot:run`. Porta padrão: **8080**.

## Config

Tudo em `src/main/resources/application.properties`: datasource, Redis, segredo JWT (troque em produção), URLs de câmbio e TTL do cache.

## Endpoints úteis do desafio

- `POST /auth/login` — corpo `{"username":"admin","password":"admin"}` (ou `user`/`user`).
- `GET /veiculos/relatorios/por-marca` — JSON do relatório por marca (existe também `/relatorios/marca` como alias).
- `GET /veiculos/relatorios/por-marca/pdf` — PDF (header `Authorization: Bearer …`).

Swagger: **http://localhost:8080/swagger-ui.html**

## Testes e cobertura

Perfil `test` usa H2 em memória e moca o Redis. Integração cobre fluxo com JWT; há teste do PDF com Jasper em contexto completo.

```bash
./mvnw test
./mvnw verify
```

(`mvnw.cmd` no Windows.)

O `verify` gera o JaCoCo em `target/site/jacoco/` e falha se a cobertura de linhas do bundle ficar abaixo do mínimo configurado no `pom.xml`.
