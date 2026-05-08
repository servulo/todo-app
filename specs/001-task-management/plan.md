# Implementation Plan: Criação e Gerenciamento de Tarefas

**Branch**: `001-task-management` | **Date**: 2026-05-06 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `specs/001-task-management/spec.md`

## Summary

Implementar o CRUD de tarefas no backend Quarkus (criar, atualizar status/campos,
excluir, visualizar) e as telas correspondentes no frontend Angular. Cada tarefa
pertence a um usuário identificado pelo JWT RSA emitido pelo `auth-app`. O backend
`todo-app` valida o JWT em cada requisição — nunca emite tokens. A data de conclusão
é registrada automaticamente ao transicionar para status `CONCLUIDA` e zerada ao
retornar a um status ativo.

## Technical Context

**Language/Version**: Java 21 LTS (backend); TypeScript 5.x strict mode (frontend)

**Primary Dependencies**:
- Backend: Quarkus 3.x LTS, RESTEasy Reactive, Hibernate ORM Panache, SmallRye JWT,
  SmallRye OpenAPI, Hibernate Validator, JDBC SQL Server (quarkus-jdbc-mssql)
- Frontend: Angular 18 LTS, Angular HttpClient, Angular Reactive Forms
- Testes backend: JUnit 5, RestAssured, Quarkus Test Framework, Testcontainers
  (mssql), AssertJ

**Storage**: SQL Server (produção); SQL Server Docker Container via Testcontainers
em testes de integração

**Testing**: JUnit 5 + RestAssured + Quarkus Test Framework (backend);
Jest (frontend unitários)

**Target Platform**: Linux server (Docker container)

**Project Type**: web-service (REST API) + web-app (frontend SPA)

**Performance Goals**: Operações CRUD com resposta < 500ms p95 em condições normais
de carga

**Constraints**:
- Todos os endpoints do todo-app DEVEM exigir JWT válido (HTTP 401 se ausente/expirado)
- Isolamento total de dados entre usuários (userId extraído do claim `sub` do JWT)
- Hard delete — sem soft delete / lixeira

**Scale/Scope**: Escopo desta feature: CRUD individual de tarefas (sem listagem).
Sistema single-tenant para uso pessoal / equipe pequena.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Gate | Status | Evidência |
|-----------|------|--------|-----------|
| **I. API-First** | Todos os endpoints documentados com OpenAPI antes da implementação | ✅ PASS | Contratos definidos em `contracts/task-api.md` e `contracts/auth-api.md` |
| **I. API-First** | Spec OpenAPI versionada no source | ✅ PASS | Gerada automaticamente pelo Quarkus em `/q/openapi` e comitada |
| **II. Separação** | Lógica de negócio no backend; Angular só chama API | ✅ PASS | Automação de `completedAt` no `TaskService`; Angular usa `TaskService` HTTP |
| **II. Separação** | Backend testável independente do frontend | ✅ PASS | Testes RestAssured cobrem todos os endpoints sem Angular |
| **III. Test-First** | Testes de contrato escritos antes dos endpoints | ✅ PASS | `tasks.md` exigirá testes ANTES da implementação por US |
| **III. Test-First** | Integração cobre fluxos críticos de cada US | ✅ PASS | Testes planejados para US1–US4 com Testcontainers SQL Server |
| **IV. Observabilidade** | Logs JSON com correlationId | ✅ PASS | Configurado via `quarkus.log.console.json=true` |
| **IV. Observabilidade** | Health check `/q/health` (liveness + readiness) | ✅ PASS | SmallRye Health incluso; readiness probe verifica DB |
| **V. Simplicidade** | Sem abstrações desnecessárias | ✅ PASS | Panache Active Record direto — sem Repository pattern |
| **V. Simplicidade** | YAGNI | ✅ PASS | Sem soft delete, auditoria, versionamento ou cache |
| **Autenticação** | todo-app NUNCA emite tokens — só valida | ✅ PASS | SmallRye JWT configurado apenas para validação (chave pública RSA) |
| **Autenticação** | Identity via claim `sub` do JWT | ✅ PASS | `userId` extraído de `@Claim(Claims.sub)` injetado por CDI |

**Post-Phase-1 Re-check**: ✅ Todos os gates mantidos após design.

## Project Structure

### Documentation (this feature)

```text
specs/001-task-management/
├── plan.md              # Este arquivo
├── research.md          # Decisões técnicas fundamentadas
├── data-model.md        # Modelo de dados da entidade Task
├── quickstart.md        # Como rodar e testar localmente
├── contracts/
│   ├── task-api.md      # Contrato REST dos endpoints de tarefas (todo-app)
│   └── auth-api.md      # Contrato do endpoint de login (auth-app, consumido pelo frontend)
└── tasks.md             # Tarefas de implementação (gerado por /speckit-tasks)
```

### Source Code (repository root)

```text
backend/                          (Quarkus — Maven project)
├── src/
│   ├── main/
│   │   ├── java/br/com/todoapp/
│   │   │   ├── task/
│   │   │   │   ├── Task.java                  (entidade Panache)
│   │   │   │   ├── TaskStatus.java            (enum)
│   │   │   │   ├── TaskPriority.java          (enum)
│   │   │   │   ├── TaskResource.java          (endpoint REST)
│   │   │   │   ├── TaskService.java           (lógica de negócio)
│   │   │   │   ├── CreateTaskRequest.java
│   │   │   │   ├── UpdateTaskRequest.java
│   │   │   │   ├── UpdateStatusRequest.java
│   │   │   │   └── TaskResponse.java
│   │   │   └── security/
│   │   │       └── UserContext.java           (extrai sub do JWT)
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/br/com/todoapp/
│           ├── task/
│           │   ├── TaskResourceContractTest.java
│           │   ├── TaskResourceIT.java
│           │   └── TaskServiceTest.java
│           └── util/
│               └── SqlServerTestResource.java
└── pom.xml

frontend/                         (Angular — npm project)
├── src/app/
│   ├── auth/
│   │   ├── login/
│   │   │   ├── login.component.ts         (formulário de login)
│   │   │   └── login.component.html
│   │   ├── auth.service.ts                (chama auth-app, armazena JWT)
│   │   └── auth.guard.ts                  (redireciona não autenticados)
│   ├── tasks/
│   │   ├── task.model.ts
│   │   ├── task.service.ts                (HTTP calls para todo-app)
│   │   ├── task-create/
│   │   │   ├── task-create.component.ts
│   │   │   └── task-create.component.html
│   │   ├── task-detail/
│   │   │   ├── task-detail.component.ts
│   │   │   └── task-detail.component.html
│   │   └── task-update-status/
│   │       ├── task-update-status.component.ts
│   │       └── task-update-status.component.html
│   └── shared/
│       └── interceptors/
│           └── auth.interceptor.ts        (injeta Bearer token em todas as requisições)
└── package.json
```

**Structure Decision**: Web application com backend Quarkus (Maven) e frontend Angular
(npm) em diretórios separados no mesmo repositório. O `auth-app` é um serviço externo
— não faz parte deste repositório.

## Complexity Tracking

> **Nenhuma violação dos princípios constitucionais — seção vazia.**
