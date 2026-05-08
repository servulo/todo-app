# Todo App

Aplicação de gerenciamento de tarefas construída com **Quarkus** no backend e **Angular** no frontend, utilizando autenticação via JWT com assinatura RSA.

## Como foi construído

O projeto foi desenvolvido com o auxílio do fluxo **Speckit** — uma metodologia de desenvolvimento guiada por especificação. Antes de qualquer linha de código, foram produzidos artefatos de design: especificação funcional, plano técnico, modelo de dados e contratos de API. A partir desses documentos, um plano de tarefas detalhado foi gerado e seguido com rigor, adotando a abordagem **Test-First**: os testes foram escritos e validados como falhos antes da implementação de cada funcionalidade.

## Stack

### Backend

| Tecnologia | Versão | Papel |
|---|---|---|
| Java | 21 LTS | Linguagem principal |
| Quarkus | 3.8.6 LTS | Framework backend |
| Hibernate ORM Panache | — | Persistência (Active Record) |
| SmallRye JWT | — | Validação de tokens RSA |
| SmallRye OpenAPI | — | Documentação automática da API |
| SmallRye Health | — | Health check com probe de banco |
| Hibernate Validator | — | Validação de entrada |
| SQL Server 2022 | — | Banco de dados relacional |
| Testcontainers | — | SQL Server em container para testes |

### Frontend

| Tecnologia | Versão | Papel |
|---|---|---|
| Angular | 18 LTS | Framework SPA |
| TypeScript | 5.x | Linguagem principal |
| Reactive Forms | — | Formulários reativos |
| HttpClient | — | Comunicação com a API |
| Jest + jest-preset-angular | 29 / 14 | Testes unitários |

### Infraestrutura

| Tecnologia | Papel |
|---|---|
| Docker / Docker Compose | Ambiente local de desenvolvimento |
| JWT (RSA 2048) | Autenticação stateless entre serviços |

## Arquitetura

A autenticação é **stateless com JWT**. Um serviço externo (`auth-app`) é responsável por emitir tokens assinados com chave privada RSA. O `todo-app` apenas valida os tokens com a chave pública correspondente — nunca emite tokens próprios.

```
Usuário → Frontend (Angular)
             ↓ POST /api/auth/login
          auth-app  →  JWT assinado (RSA)
             ↓ Bearer <token>
          todo-app  →  Valida JWT → Executa operação
```

Cada tarefa pertence ao usuário identificado pelo claim `sub` do JWT. Tentativas de acessar tarefas de outros usuários retornam **404** (não 403), evitando vazamento de informação sobre a existência do recurso.

## Como rodar localmente

**Pré-requisitos:** Java 21+, Node.js 20+, Maven 3.9+, Docker

```bash
# 1. Subir o banco de dados
docker compose up -d

# 2. Criar o banco (primeira vez)
docker exec todo-app-sqlserver-1 \
  /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa \
  -P "YourStrong@Passw0rd" -C \
  -Q "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name='tododb') CREATE DATABASE tododb"

# 3. Iniciar o backend (http://localhost:8080)
cd backend
mvn quarkus:dev

# 4. Iniciar o frontend (http://localhost:4200)
cd frontend
npm install
npm start
```

A documentação da API estará disponível em `http://localhost:8080/q/swagger-ui`.
