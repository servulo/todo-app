# Research: Criação e Gerenciamento de Tarefas

**Feature**: 001-task-management
**Date**: 2026-05-06

## Decisão 1 — Validação de JWT no Quarkus

**Decision**: Usar `quarkus-smallrye-jwt` com chave pública RSA configurada via
`mp.jwt.verify.publickey.location`.

**Rationale**: SmallRye JWT é a extensão nativa do Quarkus para MicroProfile JWT.
Suporta validação RSA out-of-the-box com zero código boilerplate — basta anotar o
endpoint com `@Authenticated` e injetar o `JsonWebToken` ou claims individuais via
`@Claim`. A chave pública RSA do `auth-app` é fornecida como variável de ambiente,
alinhado com a restrição constitucional de configuração via env vars.

**Alternatives considered**:
- Nimbus JOSE JWT (biblioteca standalone): mais controle, mas requer filtro manual e
  quebra a integração com CDI — viola o princípio de Simplicidade.
- Keycloak adapter: acoplamento desnecessário a um produto específico, fora do escopo.

---

## Decisão 2 — Identificação do Usuário

**Decision**: Extrair o `userId` do claim `sub` (subject) do JWT via
`@Inject @Claim(standard = Claims.sub) String userId` em `UserContext`.

**Rationale**: O claim `sub` é padrão JWT (RFC 7519) e é o identificador único
emitido pelo `auth-app`. Armazenar como `String` na entidade `Task` (coluna
`user_id`) desacopla o `todo-app` do modelo de usuário do `auth-app` — não há
foreign key para uma tabela de usuários inexistente neste serviço.

**Alternatives considered**:
- UUID convertido do `sub`: complexidade desnecessária se o `auth-app` já emite UUID.
- Long sequencial: viola isolamento entre serviços, exige tabela de usuários local.

---

## Decisão 3 — ID da Entidade Task

**Decision**: UUID v4 gerado pelo banco (`DEFAULT NEWID()`) ou pela aplicação
(`UUID.randomUUID()`), armazenado como `UNIQUEIDENTIFIER` no SQL Server.

**Rationale**: UUID evita enumeração sequencial de IDs (segurança), é compatível
com geração distribuída e é idiomático em Panache com `@GeneratedValue`. No SQL
Server, `UNIQUEIDENTIFIER` é o tipo nativo.

**Alternatives considered**:
- `BIGINT IDENTITY`: mais eficiente em índices clustered, mas expõe contagem total
  de registros e viola boas práticas de segurança para IDs expostos na API.

---

## Decisão 4 — Persistência com SQL Server no Quarkus

**Decision**: Usar `quarkus-jdbc-mssql` (Microsoft JDBC driver empacotado pelo
Quarkus) com Hibernate ORM Panache (Active Record pattern).

**Rationale**: `quarkus-jdbc-mssql` inclui o driver Microsoft JDBC e se integra
automaticamente com o DataSource configurado via `quarkus.datasource.*`. Panache
Active Record (`PanacheEntity`) elimina a necessidade de repositório separado,
alinhado com o princípio de Simplicidade.

**Alternatives considered**:
- Panache Repository pattern: útil quando a entidade precisa ser compartilhada entre
  múltiplos repositórios — desnecessário aqui, violaria YAGNI.

---

## Decisão 5 — SQL Server em Testes com Testcontainers

**Decision**: Usar `testcontainers-mssqlserver` com `@QuarkusTestResource` para
subir SQL Server em Docker durante testes de integração.

**Rationale**: Garante paridade de engine entre testes e produção (constituição v1.0.3+).
Testcontainers gerencia o ciclo de vida do container automaticamente. O Quarkus
suporta test resources nativamente.

**Image**: `mcr.microsoft.com/mssql/server:2022-latest` (imagem oficial Microsoft).

**Alternatives considered**:
- Docker Compose externo: requer setup manual antes dos testes, frágil em CI.
- H2 com dialeto SQL Server: não replica comportamento real; foi substituído pela
  constituição v1.0.3.

---

## Decisão 6 — Datas e Fusos Horários

**Decision**: `LocalDateTime` para `createdAt` e `completedAt`; `LocalDate` para
`dueDate`. Armazenados sem fuso horário no banco (SQL Server `DATETIME2` e `DATE`).

**Rationale**: A feature não define requisitos de fuso horário multi-região. Usar
UTC implícito via configuração do servidor. Manter simples (Princípio V).

**Alternatives considered**:
- `OffsetDateTime` / `ZonedDateTime`: correto para sistemas multi-região, mas
  adiciona complexidade desnecessária para o escopo atual (YAGNI).

---

## Decisão 7 — Atualização de Campos (PUT vs PATCH)

**Decision**: Dois endpoints distintos:
- `PUT /api/tasks/{id}` — atualiza campos editáveis (título, descrição, prioridade,
  data limite)
- `PATCH /api/tasks/{id}/status` — atualiza exclusivamente o status (e dispara
  lógica de `completedAt`)

**Rationale**: Separar a atualização de status em endpoint dedicado permite:
(a) aplicar a lógica de `completedAt` de forma isolada e testável,
(b) deixar claro no contrato que status tem comportamento especial,
(c) evitar condições de corrida ao atualizar status + outros campos simultaneamente.

**Alternatives considered**:
- `PUT` único com todos os campos: mistura campos editáveis e transições de estado,
  complica testes de contrato e validação.
- `PATCH` genérico (JSON Patch / Merge Patch): complexidade desnecessária para
  o escopo atual.

---

## Decisão 8 — Estrutura de Pacotes

**Decision**: Pacote base `br.com.todoapp`, organizado por feature slice:
`br.com.todoapp.task.*` e `br.com.todoapp.security.*`.

**Rationale**: Feature slices facilitam coesão e navegação. Para o escopo atual
(uma entidade), uma estrutura técnica (models/services/resources) seria equivalente,
mas slices escalam melhor para features futuras.
