# Data Model: Criação e Gerenciamento de Tarefas

**Feature**: 001-task-management
**Date**: 2026-05-06

## Entidade: Task

### Atributos

| Campo        | Tipo Java        | Tipo SQL Server    | Obrigatório | Gerado por  | Observações                                           |
|--------------|------------------|--------------------|-------------|-------------|-------------------------------------------------------|
| `id`         | `UUID`           | `UNIQUEIDENTIFIER` | Sim         | Aplicação   | PK, gerado via `UUID.randomUUID()`                    |
| `title`      | `String`         | `NVARCHAR(255)`    | Sim         | Usuário     | Não pode ser branco/vazio                             |
| `description`| `String`         | `NVARCHAR(MAX)`    | Não         | Usuário     | Texto livre, nullable                                 |
| `priority`   | `TaskPriority`   | `NVARCHAR(10)`     | Não         | Usuário     | Default: `MEDIA`; valores: BAIXA, MEDIA, ALTA         |
| `status`     | `TaskStatus`     | `NVARCHAR(10)`     | Sim         | Sistema     | Default: `CRIADA` na criação; valores: CRIADA, ANDAMENTO, CONCLUIDA |
| `dueDate`    | `LocalDate`      | `DATE`             | Não         | Usuário     | Nullable; datas passadas são aceitas                  |
| `createdAt`  | `LocalDateTime`  | `DATETIME2`        | Sim         | Sistema     | Definido na criação, imutável                         |
| `completedAt`| `LocalDateTime`  | `DATETIME2`        | Não         | Sistema     | Definido ao → `CONCLUIDA`; zerado ao ← `CONCLUIDA`   |
| `userId`     | `String`         | `NVARCHAR(255)`    | Sim         | Sistema     | Extraído do claim `sub` do JWT; indexado               |

### Enums

**TaskStatus**:
```
CRIADA      — estado inicial de toda tarefa
ANDAMENTO   — tarefa em progresso
CONCLUIDA   — tarefa finalizada (registra completedAt)
```

**TaskPriority**:
```
BAIXA
MEDIA    — padrão quando não informado
ALTA
```

### Regras de Negócio

1. **Criação**: `status` sempre `CRIADA`; `createdAt` = now(); `completedAt` = null.
2. **Transição → CONCLUIDA**: `completedAt` = now().
3. **Transição ← CONCLUIDA** (para CRIADA ou ANDAMENTO): `completedAt` = null.
4. **Transição entre outros estados**: `completedAt` não é alterado.
5. **Isolamento**: Toda operação de leitura, atualização ou exclusão DEVE verificar
   que `userId` da tarefa == `sub` do JWT. Caso contrário → HTTP 404 (não revelar
   existência).

### Diagrama

```
┌──────────────────────────────────────────────┐
│                    TASK                       │
├──────────────────────────────────────────────┤
│  id            UUID            PK             │
│  title         NVARCHAR(255)   NOT NULL       │
│  description   NVARCHAR(MAX)   NULL           │
│  priority      NVARCHAR(10)    NOT NULL       │
│                DEFAULT 'MEDIA'                │
│  status        NVARCHAR(10)    NOT NULL       │
│                DEFAULT 'CRIADA'               │
│  due_date      DATE            NULL           │
│  created_at    DATETIME2       NOT NULL       │
│  completed_at  DATETIME2       NULL           │
│  user_id       NVARCHAR(255)   NOT NULL       │
├──────────────────────────────────────────────┤
│  INDEX idx_task_user_id (user_id)             │
└──────────────────────────────────────────────┘
```

### DDL SQL Server (referência)

```sql
CREATE TABLE task (
    id            UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    title         NVARCHAR(255)    NOT NULL,
    description   NVARCHAR(MAX)    NULL,
    priority      NVARCHAR(10)     NOT NULL DEFAULT 'MEDIA',
    status        NVARCHAR(10)     NOT NULL DEFAULT 'CRIADA',
    due_date      DATE             NULL,
    created_at    DATETIME2        NOT NULL,
    completed_at  DATETIME2        NULL,
    user_id       NVARCHAR(255)    NOT NULL,
    CONSTRAINT pk_task PRIMARY KEY (id),
    CONSTRAINT chk_task_priority CHECK (priority IN ('BAIXA', 'MEDIA', 'ALTA')),
    CONSTRAINT chk_task_status   CHECK (status   IN ('CRIADA', 'ANDAMENTO', 'CONCLUIDA'))
);

CREATE INDEX idx_task_user_id ON task (user_id);
```

### Mapeamento Hibernate (referência)

```java
@Entity
@Table(name = "task")
public class Task extends PanacheEntityBase {

    @Id
    public UUID id = UUID.randomUUID();

    @NotBlank
    @Column(nullable = false, length = 255)
    public String title;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    public String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    public TaskPriority priority = TaskPriority.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    public TaskStatus status = TaskStatus.CRIADA;

    @Column(name = "due_date")
    public LocalDate dueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @Column(name = "user_id", nullable = false, length = 255)
    public String userId;
}
```
