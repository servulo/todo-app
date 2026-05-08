# Tasks: Criação e Gerenciamento de Tarefas

**Input**: Design documents from `specs/001-task-management/`
**Prerequisites**: plan.md ✅ spec.md ✅ data-model.md ✅ contracts/ ✅ research.md ✅

**⚠️ Test-First Obrigatório (Constituição — Princípio III NÃO NEGOCIÁVEL)**:
Dentro de cada User Story, os testes DEVEM ser escritos e verificados como FAILING
antes que qualquer tarefa de implementação seja iniciada.
Ciclo: **Escrever testes → Confirmar que falham (RED) → Implementar (GREEN) → Refatorar**

**Organization**: Tasks grouped by User Story for independent implementation and testing.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (US1–US5)
- Include exact file paths in all task descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization — must complete before Phase 2

- [X] T001 Create Quarkus Maven project with extensions (quarkus-resteasy-reactive-jackson, quarkus-hibernate-orm-panache, quarkus-jdbc-mssql, quarkus-smallrye-jwt, quarkus-smallrye-openapi, quarkus-smallrye-health, quarkus-hibernate-validator) in backend/pom.xml
- [X] T002 Create Angular 18 project with strict TypeScript and HttpClient in frontend/ (ng new todo-app-frontend --strict --routing)
- [X] T003 [P] Configure backend datasource, JWT public key location, JSON logging and health check in backend/src/main/resources/application.properties
- [X] T004 [P] Configure frontend environments with apiUrl and authApiUrl in frontend/src/environments/environment.ts and environment.prod.ts

**Checkpoint**: Projects initialized — Phase 2 can begin

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure required by ALL user stories

**⚠️ CRITICAL**: No User Story work can begin until this phase is complete

- [X] T005 Create docker-compose.yml at repository root with SQL Server 2022 service for local development
- [X] T006 Create SqlServerTestResource (QuarkusTestResource + Testcontainers mssql) in backend/src/test/java/br/com/todoapp/util/SqlServerTestResource.java
- [X] T007 [P] Create TaskStatus enum (CRIADA, ANDAMENTO, CONCLUIDA) in backend/src/main/java/br/com/todoapp/task/TaskStatus.java
- [X] T008 [P] Create TaskPriority enum (BAIXA, MEDIA, ALTA) in backend/src/main/java/br/com/todoapp/task/TaskPriority.java
- [X] T009 Create Task Panache entity with all fields (id UUID, title, description, priority, status, dueDate, createdAt, completedAt, userId) per data-model.md in backend/src/main/java/br/com/todoapp/task/Task.java
- [X] T010 [P] Create UserContext CDI bean injecting @Claim(standard = Claims.sub) in backend/src/main/java/br/com/todoapp/security/UserContext.java
- [X] T010b [P] Write AuthInterceptor unit tests in frontend/src/app/shared/interceptors/auth.interceptor.spec.ts covering: JWT present in localStorage → Authorization: Bearer header attached; JWT absent → header not attached; HTTP 401 response from todo-app → redirect to /login
- [X] T011 [P] Create AuthInterceptor (HttpInterceptorFn) that reads JWT from localStorage and attaches Authorization header in frontend/src/app/shared/interceptors/auth.interceptor.ts

**Checkpoint**: Foundation ready — all User Story phases can now begin in priority order

---

## Phase 3: User Story 1 — Fazer Login (Priority: P1) 🎯 MVP

**Goal**: User can authenticate via the `auth-app` endpoint; JWT is stored and attached automatically to all subsequent requests.

**Independent Test**: Navigate to /login, submit valid credentials → JWT stored in localStorage, redirect to /tasks. Submit invalid credentials → error shown, no redirect.

### ⚠️ Tests FIRST — confirm RED before implementing

- [X] T012 [P] [US1] Write AuthService unit tests covering: successful login stores JWT, 401 response surfaces error, correct POST to authApiUrl in frontend/src/app/auth/auth.service.spec.ts
- [X] T013 [P] [US1] Write LoginComponent unit tests covering: empty fields show validation error without API call, successful login triggers redirect, 401 response shows "Credenciais inválidas", authenticated user is redirected away from /login in frontend/src/app/auth/login/login.component.spec.ts

### Implementation for User Story 1

- [X] T014 [US1] Implement AuthService with login(), logout(), isAuthenticated() and JWT localStorage management in frontend/src/app/auth/auth.service.ts
- [X] T014b [US1] Write AuthGuard unit tests in frontend/src/app/auth/auth.guard.spec.ts covering: unauthenticated user accessing /tasks/** is redirected to /login; authenticated user passes through to route
- [X] T015 [US1] Implement AuthGuard (CanActivateFn) redirecting unauthenticated users to /login in frontend/src/app/auth/auth.guard.ts
- [X] T016 [US1] Implement LoginComponent class with ReactiveForm (username + password), AuthService call and error state in frontend/src/app/auth/login/login.component.ts
- [X] T017 [US1] Create login template with username field, password field, error message area and submit button in frontend/src/app/auth/login/login.component.html
- [X] T018 [US1] Configure app routing: /login → LoginComponent (public); / and /tasks/** → protected with AuthGuard in frontend/src/app/app.routes.ts

**Checkpoint**: US1 fully functional and independently testable — user can log in, JWT is stored and attached to requests

---

## Phase 4: User Story 2 — Criar Nova Tarefa (Priority: P2) 🎯 Backend MVP

**Goal**: Authenticated user creates a task with title (required), description, priority, and due date; system persists it with status CRIADA and records createdAt.

**Independent Test**: POST /api/tasks with valid JWT and body → 201, task in DB with status=CRIADA, createdAt set. POST without title → 400. POST without JWT → 401.

### ⚠️ Tests FIRST — confirm RED before implementing

- [X] T019 [P] [US2] Write contract tests for POST /api/tasks: 201 on valid request, 400 on blank title, 400 on invalid priority, 401 on missing JWT, 201 when dueDate is in the past (retroactive tasks are valid) in backend/src/test/java/br/com/todoapp/task/TaskResourceContractTest.java
- [X] T020 [P] [US2] Write integration tests for task creation using Testcontainers SQL Server: verifies persistence, status=CRIADA, createdAt populated, completedAt null in backend/src/test/java/br/com/todoapp/task/TaskResourceIT.java
- [X] T021 [P] [US2] Write TaskService unit tests for create(): status forced to CRIADA, createdAt = now(), completedAt = null, userId set from JWT in backend/src/test/java/br/com/todoapp/task/TaskServiceTest.java

### Implementation for User Story 2

- [X] T022 [P] [US2] Create CreateTaskRequest DTO with @NotBlank title, optional description, optional priority (default MEDIA), optional dueDate in backend/src/main/java/br/com/todoapp/task/CreateTaskRequest.java
- [X] T023 [P] [US2] Create TaskResponse DTO mapping all Task fields to JSON-serializable types in backend/src/main/java/br/com/todoapp/task/TaskResponse.java
- [X] T024 [US2] Implement TaskService.create() enforcing status=CRIADA, createdAt=now(), userId from UserContext in backend/src/main/java/br/com/todoapp/task/TaskService.java
- [X] T025 [US2] Implement POST /api/tasks endpoint with @Authenticated and @Operation OpenAPI annotation in backend/src/main/java/br/com/todoapp/task/TaskResource.java
- [X] T026 [P] [US2] Write TaskService HTTP unit tests for create() method in frontend/src/app/tasks/task.service.spec.ts
- [X] T027 [P] [US2] Write TaskCreateComponent unit tests covering: valid form submits, blank title shows error, API error displayed in frontend/src/app/tasks/task-create/task-create.component.spec.ts
- [X] T028 [US2] Create task.model.ts with Task, CreateTaskRequest and TaskStatus/TaskPriority types in frontend/src/app/tasks/task.model.ts
- [X] T029 [US2] Implement TaskService.create() HTTP POST to /api/tasks in frontend/src/app/tasks/task.service.ts
- [X] T030 [US2] Implement TaskCreateComponent with ReactiveForm (title required, description, priority select, dueDate) in frontend/src/app/tasks/task-create/task-create.component.ts
- [X] T031 [US2] Create task-create template with all form fields and validation messages in frontend/src/app/tasks/task-create/task-create.component.html
- [X] T032 [US2] Add /tasks/new route protected by AuthGuard in frontend/src/app/app.routes.ts

**Checkpoint**: US2 fully functional — authenticated user can create a task end-to-end

---

## Phase 5: User Story 3 — Atualizar Status da Tarefa (Priority: P3)

**Goal**: User updates task status; transitioning to CONCLUIDA auto-sets completedAt; transitioning away from CONCLUIDA clears it.

**Independent Test**: Create task (US2 backend), PATCH /status → CONCLUIDA → completedAt set. PATCH /status → ANDAMENTO → completedAt cleared. PUT → updates other fields.

### ⚠️ Tests FIRST — confirm RED before implementing

- [X] T033 [P] [US3] Add contract tests for PATCH /api/tasks/{id}/status (200 valid, 400 invalid status, 404 wrong owner/not found, 401) and PUT /api/tasks/{id} (200 valid, 400 blank title, 404) in backend/src/test/java/br/com/todoapp/task/TaskResourceContractTest.java
- [X] T034 [P] [US3] Add integration tests for status transitions: CRIADA→ANDAMENTO (completedAt null), ANDAMENTO→CONCLUIDA (completedAt set), CONCLUIDA→ANDAMENTO (completedAt cleared), cross-user ownership returns 404 in backend/src/test/java/br/com/todoapp/task/TaskResourceIT.java
- [X] T035 [P] [US3] Add TaskService unit tests for update() and updateStatus() covering completedAt logic and ownership enforcement in backend/src/test/java/br/com/todoapp/task/TaskServiceTest.java

### Implementation for User Story 3

- [X] T036 [P] [US3] Create UpdateTaskRequest DTO (title required, description nullable, priority required, dueDate nullable) in backend/src/main/java/br/com/todoapp/task/UpdateTaskRequest.java
- [X] T037 [P] [US3] Create UpdateStatusRequest DTO with @NotNull status field in backend/src/main/java/br/com/todoapp/task/UpdateStatusRequest.java
- [X] T038 [US3] Implement TaskService.update() (ownership check, field update) and TaskService.updateStatus() (completedAt logic per data-model.md rules 2–4) in backend/src/main/java/br/com/todoapp/task/TaskService.java
- [X] T039 [US3] Add PUT /api/tasks/{id} and PATCH /api/tasks/{id}/status endpoints with OpenAPI annotations in backend/src/main/java/br/com/todoapp/task/TaskResource.java
- [X] T040 [P] [US3] Write TaskUpdateStatusComponent unit tests covering status dropdown, successful update and error handling in frontend/src/app/tasks/task-update-status/task-update-status.component.spec.ts
- [X] T041 [US3] Add TaskService.updateStatus() and TaskService.updateFields() HTTP methods in frontend/src/app/tasks/task.service.ts
- [X] T042 [US3] Implement TaskUpdateStatusComponent with status select (CRIADA/ANDAMENTO/CONCLUIDA) in frontend/src/app/tasks/task-update-status/task-update-status.component.ts
- [X] T043 [US3] Create task-update-status template with status select and save button in frontend/src/app/tasks/task-update-status/task-update-status.component.html

**Checkpoint**: US3 fully functional — status transitions work, completedAt automated

---

## Phase 6: User Story 4 — Excluir Tarefa (Priority: P4)

**Goal**: User permanently deletes a task they own; deleted task returns 404 on subsequent access; other users' tasks return 404 without revealing existence.

**Independent Test**: Create task, DELETE → 204, then GET → 404. DELETE other user's task → 404.

### ⚠️ Tests FIRST — confirm RED before implementing

- [X] T044 [P] [US4] Add contract tests for DELETE /api/tasks/{id}: 204 on success, 404 on non-existent, 404 on wrong owner (not 403), 401 on no JWT in backend/src/test/java/br/com/todoapp/task/TaskResourceContractTest.java
- [X] T045 [P] [US4] Add integration tests for hard delete: DELETE returns 204, subsequent GET returns 404; another userId's task returns 404 on DELETE attempt in backend/src/test/java/br/com/todoapp/task/TaskResourceIT.java
- [X] T046 [P] [US4] Add TaskService unit test for delete() covering ownership check (wrong user → NotFoundException) in backend/src/test/java/br/com/todoapp/task/TaskServiceTest.java

### Implementation for User Story 4

- [X] T047 [US4] Implement TaskService.delete() with userId ownership check, throws NotFoundException (mapped to 404) if not found or wrong owner in backend/src/main/java/br/com/todoapp/task/TaskService.java
- [X] T048 [US4] Add DELETE /api/tasks/{id} endpoint returning 204 No Content with OpenAPI annotation in backend/src/main/java/br/com/todoapp/task/TaskResource.java
- [X] T049 [P] [US4] Write unit tests for delete functionality in TaskDetailComponent: delete button visible, confirmation flow, 204 redirects, error displayed in frontend/src/app/tasks/task-detail/task-detail.component.spec.ts
- [X] T050 [US4] Add TaskService.delete() HTTP DELETE method in frontend/src/app/tasks/task.service.ts
- [X] T051 [US4] Add delete button and handler to TaskDetailComponent class with post-delete navigation in frontend/src/app/tasks/task-detail/task-detail.component.ts

**Checkpoint**: US4 fully functional — tasks can be permanently deleted

---

## Phase 7: User Story 5 — Visualizar Detalhes da Tarefa (Priority: P5)

**Goal**: User views all task fields including completedAt when present; non-existent or other-user tasks return 404.

**Independent Test**: Create task (US2), GET /api/tasks/{id} → 200 with all fields. Completed task shows completedAt. Wrong ID → 404.

### ⚠️ Tests FIRST — confirm RED before implementing

- [X] T052 [P] [US5] Add contract tests for GET /api/tasks/{id}: 200 with all response fields, 404 on not found, 404 on wrong owner, 401 on no JWT in backend/src/test/java/br/com/todoapp/task/TaskResourceContractTest.java
- [X] T053 [P] [US5] Add integration tests for task retrieval: all fields present in response, completedAt populated for CONCLUIDA tasks, completedAt null for others in backend/src/test/java/br/com/todoapp/task/TaskResourceIT.java
- [X] T054 [P] [US5] Add TaskService unit test for findById() covering not-found and wrong-owner scenarios in backend/src/test/java/br/com/todoapp/task/TaskServiceTest.java

### Implementation for User Story 5

- [X] T055 [US5] Implement TaskService.findById() with ownership check in backend/src/main/java/br/com/todoapp/task/TaskService.java
- [X] T056 [US5] Add GET /api/tasks/{id} endpoint with OpenAPI annotation and TaskResponse return in backend/src/main/java/br/com/todoapp/task/TaskResource.java
- [X] T057 [P] [US5] Write TaskDetailComponent unit tests: all fields displayed, completedAt shown conditionally, 404 error handled in frontend/src/app/tasks/task-detail/task-detail.component.spec.ts
- [X] T058 [US5] Add TaskService.getById() HTTP GET method in frontend/src/app/tasks/task.service.ts
- [X] T059 [US5] Implement TaskDetailComponent class loading task by route param, exposing all fields and delete handler in frontend/src/app/tasks/task-detail/task-detail.component.ts
- [X] T060 [US5] Create task-detail template displaying all fields, conditional completedAt, delete button from US4 in frontend/src/app/tasks/task-detail/task-detail.component.html
- [X] T061 [US5] Add /tasks/:id route protected by AuthGuard in frontend/src/app/app.routes.ts

**Checkpoint**: All User Stories independently functional and testable

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Hardening that improves all stories without changing behavior

- [X] T062 [P] Enhance AuthInterceptor to catch HTTP 401 responses from todo-app and redirect to /login (token expiry handling) in frontend/src/app/shared/interceptors/auth.interceptor.ts
- [X] T063a [P] Implement CorrelationIdFilter (ContainerRequestFilter) generating UUID per request and populating MDC key "correlationId" so all log lines for a request share the same ID in backend/src/main/java/br/com/todoapp/logging/CorrelationIdFilter.java
- [X] T063 [P] Verify structured JSON logging (quarkus.log.console.json=true) and configure correlationId MDC in backend/src/main/resources/application.properties
- [X] T064 [P] Implement SmallRye Health readiness probe verifying database connectivity in backend/src/main/java/br/com/todoapp/health/ (if not auto-configured by quarkus-jdbc-mssql)
- [ ] T065 Run quickstart.md golden path validation checklist end-to-end (both login and all task operations)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 — **BLOCKS all User Stories**
- **US1 (Phase 3)**: Depends on Phase 2 — frontend only, no backend dependency
- **US2 (Phase 4)**: Depends on Phase 2 — backend + frontend; frontend also benefits from US1 done
- **US3 (Phase 5)**: Depends on Phase 2 and US2 backend (uses Task entity and TaskService base)
- **US4 (Phase 6)**: Depends on Phase 2 and US2 backend (same reasoning)
- **US5 (Phase 7)**: Depends on Phase 2 and US2 backend
- **Polish (Phase 8)**: Depends on all User Stories complete

### User Story Dependencies

- **US1**: Frontend only — no backend dependency except JWT validation already set up in Phase 2
- **US2**: First backend story — establishes TaskService, TaskResource, DTOs, test infrastructure
- **US3, US4, US5**: Add to existing TaskService and TaskResource — each independent of each other

### Within Each User Story

1. Write tests → Confirm they FAIL (RED)
2. Run: `mvn test` (backend) / `npm test` (frontend) — all new tests must fail
3. Implement until tests pass (GREEN)
4. Refactor if needed
5. Story complete → checkpoint validation

### Parallel Opportunities

Within Phase 2: T007, T008, T010, T011 can all run in parallel
Within Phase 3: T012, T013 (tests) run in parallel before T014–T018
Within Phase 4: T019, T020, T021 run in parallel; T022, T023 run in parallel; T026, T027 run in parallel
Within Phase 5: T033, T034, T035 run in parallel; T036, T037 run in parallel
Within Phase 6: T044, T045, T046 run in parallel
Within Phase 7: T052, T053, T054 run in parallel

---

## Parallel Example: User Story 2

```
# Step 1 — Run all tests in parallel (they must FAIL):
Task T019: Contract tests for POST /api/tasks
Task T020: Integration tests for task creation
Task T021: TaskService unit tests for create()
Task T026: Frontend TaskService unit tests
Task T027: TaskCreateComponent unit tests

# Step 2 — Run parallel implementation after tests fail:
Task T022: CreateTaskRequest DTO
Task T023: TaskResponse DTO
Task T028: task.model.ts

# Step 3 — Sequential (depends on above):
Task T024: TaskService.create() → T025: TaskResource POST endpoint
Task T029: Frontend TaskService.create() → T030: TaskCreateComponent → T031: template
```

---

## Implementation Strategy

### MVP First (US1 + US2 Backend Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: US1 (Login)
4. Complete Phase 4: US2 backend tasks (T019–T025)
5. **STOP and VALIDATE**: POST /api/tasks works with JWT; frontend login works
6. Continue with US2 frontend (T026–T032)

### Incremental Delivery

1. Phase 1 + 2 → Infrastructure ready
2. US1 → User can authenticate ✓
3. US2 → User can create tasks end-to-end ✓ **[Demo-ready MVP]**
4. US3 → User can update task status ✓
5. US4 → User can delete tasks ✓
6. US5 → User can view task details ✓

---

## Notes

- [P] = different files, no unresolved dependencies in current phase
- Constitution Principle III (Test-First) is non-negotiable: tests must be written and confirmed FAILING before any implementation task in the same story begins
- Backend test files grow incrementally: each US phase adds tests to the same `.java` files
- `userId` is always extracted from JWT `sub` claim — never from request body
- HTTP 404 (not 403) is used for ownership violations to avoid revealing task existence
