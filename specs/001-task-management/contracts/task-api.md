# API Contract: Task Management

**Service**: todo-app (backend Quarkus)
**Base path**: `/api/tasks`
**Auth**: Todos os endpoints exigem `Authorization: Bearer <JWT>` (HTTP 401 se ausente ou inválido)
**Content-Type**: `application/json`
**Date**: 2026-05-06

---

## POST /api/tasks — Criar Tarefa

**User Story**: US1 (P1)

### Request

```http
POST /api/tasks
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Estudar Java",
  "description": "Revisar generics e streams",
  "priority": "ALTA",
  "dueDate": "2026-05-30"
}
```

| Campo         | Tipo     | Obrigatório | Validações                              |
|---------------|----------|-------------|-----------------------------------------|
| `title`       | string   | Sim         | Não branco; max 255 caracteres          |
| `description` | string   | Não         | Texto livre; null se omitido            |
| `priority`    | enum     | Não         | `BAIXA` \| `MEDIA` \| `ALTA`; default `MEDIA` |
| `dueDate`     | date     | Não         | Formato `YYYY-MM-DD`; datas passadas aceitas |

### Responses

**201 Created**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Estudar Java",
  "description": "Revisar generics e streams",
  "priority": "ALTA",
  "status": "CRIADA",
  "dueDate": "2026-05-30",
  "createdAt": "2026-05-06T14:30:00",
  "completedAt": null
}
```

**400 Bad Request** — título ausente, branco ou prioridade inválida
```json
{
  "errors": [
    { "field": "title", "message": "O título é obrigatório" }
  ]
}
```

**401 Unauthorized** — JWT ausente, inválido ou expirado
```json
{ "message": "Unauthorized" }
```

---

## PUT /api/tasks/{id} — Atualizar Campos da Tarefa

**User Story**: US2 (P2) — campos editáveis (não altera status)

### Request

```http
PUT /api/tasks/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Estudar Java avançado",
  "description": "Focar em concorrência",
  "priority": "MEDIA",
  "dueDate": "2026-06-15"
}
```

| Campo         | Tipo   | Obrigatório | Validações                              |
|---------------|--------|-------------|-----------------------------------------|
| `title`       | string | Sim         | Não branco; max 255 caracteres          |
| `description` | string | Não         | null remove a descrição existente       |
| `priority`    | enum   | Sim         | `BAIXA` \| `MEDIA` \| `ALTA`           |
| `dueDate`     | date   | Não         | null remove a data limite existente     |

### Responses

**200 OK**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Estudar Java avançado",
  "description": "Focar em concorrência",
  "priority": "MEDIA",
  "status": "CRIADA",
  "dueDate": "2026-06-15",
  "createdAt": "2026-05-06T14:30:00",
  "completedAt": null
}
```

**400 Bad Request** — validação falhou
**401 Unauthorized**
**404 Not Found** — tarefa não existe ou pertence a outro usuário
```json
{ "message": "Task not found" }
```

---

## PATCH /api/tasks/{id}/status — Atualizar Status

**User Story**: US2 (P2) — transição de status com lógica de `completedAt`

### Request

```http
PATCH /api/tasks/550e8400-e29b-41d4-a716-446655440000/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "CONCLUIDA"
}
```

| Campo    | Tipo | Obrigatório | Validações                                      |
|----------|------|-------------|--------------------------------------------------|
| `status` | enum | Sim         | `CRIADA` \| `ANDAMENTO` \| `CONCLUIDA`          |

### Responses

**200 OK** — status `CONCLUIDA`, `completedAt` preenchido
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Estudar Java avançado",
  "description": "Focar em concorrência",
  "priority": "MEDIA",
  "status": "CONCLUIDA",
  "dueDate": "2026-06-15",
  "createdAt": "2026-05-06T14:30:00",
  "completedAt": "2026-05-06T16:45:00"
}
```

**400 Bad Request** — status inválido
**401 Unauthorized**
**404 Not Found**

---

## DELETE /api/tasks/{id} — Excluir Tarefa

**User Story**: US3 (P3)

### Request

```http
DELETE /api/tasks/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <token>
```

### Responses

**204 No Content** — exclusão realizada com sucesso (sem body)

**401 Unauthorized**

**404 Not Found** — tarefa não existe ou pertence a outro usuário
```json
{ "message": "Task not found" }
```

---

## GET /api/tasks/{id} — Visualizar Detalhes

**User Story**: US4 (P4)

### Request

```http
GET /api/tasks/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <token>
```

### Responses

**200 OK**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Estudar Java avançado",
  "description": "Focar em concorrência",
  "priority": "MEDIA",
  "status": "CONCLUIDA",
  "dueDate": "2026-06-15",
  "createdAt": "2026-05-06T14:30:00",
  "completedAt": "2026-05-06T16:45:00"
}
```

**401 Unauthorized**

**404 Not Found**
```json
{ "message": "Task not found" }
```

---

## Response Schema (TaskResponse)

```json
{
  "id":          "UUID string",
  "title":       "string",
  "description": "string | null",
  "priority":    "BAIXA | MEDIA | ALTA",
  "status":      "CRIADA | ANDAMENTO | CONCLUIDA",
  "dueDate":     "YYYY-MM-DD | null",
  "createdAt":   "YYYY-MM-DDTHH:MM:SS",
  "completedAt": "YYYY-MM-DDTHH:MM:SS | null"
}
```

## Comportamento de Segurança (Isolamento de Usuários)

Para `PUT`, `PATCH`, `DELETE` e `GET`:
- Se a tarefa não existe → HTTP 404
- Se a tarefa existe mas `userId != sub(JWT)` → HTTP 404 (não revelar existência)
- Nunca retornar HTTP 403 para recursos de outros usuários
