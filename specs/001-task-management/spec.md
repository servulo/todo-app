# Feature Specification: Criação e Gerenciamento de Tarefas

**Feature Branch**: `001-task-management`
**Created**: 2026-05-06
**Status**: Draft
**Input**: User description: "Permitir que o usuário crie tarefas com título, descrição, prioridade (baixa, média e alta), status (criada, andamento e concluída) e data limite. Além disso, deve haver uma data de conclusão. Tarefas podem ser excluídas."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Fazer Login (Priority: P1)

Um usuário acessa a página de login do `todo-app`, informa suas credenciais (username e
senha) e submete o formulário. O frontend chama o endpoint de login do `auth-app`,
recebe o JWT e o armazena localmente. A partir desse ponto, todas as requisições ao
`todo-app` incluem automaticamente o token no header `Authorization: Bearer <token>`.

**Why this priority**: A autenticação é o pré-requisito absoluto para qualquer outra
funcionalidade. Sem login, nenhuma operação de tarefa é possível.

**Independent Test**: Pode ser testada isoladamente preenchendo o formulário de login
com credenciais válidas e verificando que o token é armazenado e que o usuário é
redirecionado para a área autenticada.

**Acceptance Scenarios**:

1. **Given** um usuário não autenticado na página de login, **When** ele informa
   username e senha válidos e submete, **Then** o frontend recebe o JWT, armazena-o
   localmente e redireciona o usuário para a área principal da aplicação.

2. **Given** um usuário na página de login, **When** ele informa credenciais inválidas
   e submete, **Then** o sistema exibe mensagem de erro ("Credenciais inválidas") sem
   redirecionar.

3. **Given** um usuário na página de login, **When** ele submete o formulário com
   username ou senha em branco, **Then** o formulário exibe validação local sem chamar
   o `auth-app`.

4. **Given** um usuário autenticado que tenta acessar a página de login, **When** a
   página carrega, **Then** ele é redirecionado automaticamente para a área principal.

---

### User Story 2 - Criar Nova Tarefa (Priority: P2)

Um usuário autenticado preenche um formulário com os dados da tarefa (título, descrição
opcional, prioridade, data limite opcional) e submete. O sistema cria a tarefa com status
inicial "criada" e registra a data de criação.

**Why this priority**: É a capacidade central do sistema. Sem criação de tarefas, nenhuma
outra funcionalidade faz sentido. Representa o MVP mínimo viável.

**Independent Test**: Pode ser testada isoladamente criando uma tarefa via API/formulário
e verificando que ela é persistida com todos os campos corretos e status "criada".

**Acceptance Scenarios**:

1. **Given** um usuário autenticado sem tarefas, **When** ele submete o formulário com título
   "Estudar Java", prioridade "alta" e data limite 2026-05-30, **Then** a tarefa é criada
   com status "criada", data de criação preenchida e ID único gerado.

2. **Given** um usuário autenticado, **When** ele tenta criar uma tarefa sem título,
   **Then** o sistema rejeita a operação com mensagem de erro indicando que o título é
   obrigatório.

3. **Given** um usuário autenticado, **When** ele cria uma tarefa sem informar data limite,
   **Then** a tarefa é criada com sucesso e o campo data limite permanece vazio.

4. **Given** um usuário autenticado, **When** ele informa uma prioridade inválida fora das
   opções permitidas, **Then** o sistema rejeita a operação com mensagem de erro.

---

### User Story 3 - Atualizar Status da Tarefa (Priority: P3)

Um usuário altera o status de uma tarefa existente. Quando o status é alterado para
"concluída", o sistema registra automaticamente a data de conclusão.

**Why this priority**: O ciclo de vida da tarefa só tem valor se o usuário puder
progredi-la. A data de conclusão é um requisito explícito do produto.

**Independent Test**: Pode ser testada criando uma tarefa (US2) e alterando seu status,
verificando transições válidas e o preenchimento automático da data de conclusão.

**Acceptance Scenarios**:

1. **Given** uma tarefa com status "criada", **When** o usuário altera o status para
   "andamento", **Then** o status é atualizado e a data de conclusão permanece vazia.

2. **Given** uma tarefa com status "andamento", **When** o usuário altera o status para
   "concluída", **Then** o status é atualizado e a data de conclusão é registrada
   automaticamente com a data e hora atuais.

3. **Given** uma tarefa com status "concluída", **When** o usuário altera o status de
   volta para "andamento", **Then** o status é atualizado e a data de conclusão é
   removida/zerada.

---

### User Story 4 - Excluir Tarefa (Priority: P4)

Um usuário exclui permanentemente uma tarefa que lhe pertence. Após a exclusão, a tarefa
não pode mais ser recuperada.

**Why this priority**: A exclusão é necessária para que o usuário mantenha a lista de
tarefas organizada, removendo entradas desnecessárias ou criadas por engano.

**Independent Test**: Pode ser testada criando uma tarefa (US2), excluindo-a e verificando
que ela não pode mais ser consultada.

**Acceptance Scenarios**:

1. **Given** uma tarefa existente pertencente ao usuário, **When** o usuário solicita sua
   exclusão, **Then** a tarefa é removida permanentemente e não pode mais ser acessada.

2. **Given** um ID de tarefa inexistente, **When** o usuário solicita sua exclusão, **Then**
   o sistema retorna uma resposta indicando que a tarefa não foi encontrada.

3. **Given** uma tarefa pertencente a outro usuário, **When** o usuário tenta excluí-la,
   **Then** o sistema nega a operação sem revelar a existência da tarefa.

---

### User Story 5 - Visualizar Detalhes da Tarefa (Priority: P5)

Um usuário consulta os detalhes completos de uma tarefa, incluindo todos os campos
preenchidos: título, descrição, prioridade, status, data limite, data de criação e data
de conclusão (quando aplicável).

**Why this priority**: Visualizar os detalhes é essencial para acompanhamento, mas
depende das funcionalidades P2, P3 e P4 para ter valor completo.

**Independent Test**: Pode ser testada após criar uma tarefa (US2), consultando seus
dados e verificando que todos os campos são retornados corretamente.

**Acceptance Scenarios**:

1. **Given** uma tarefa existente, **When** o usuário solicita seus detalhes, **Then**
   todos os campos são exibidos: título, descrição, prioridade, status, data limite,
   data de criação e data de conclusão.

2. **Given** uma tarefa concluída, **When** o usuário consulta seus detalhes, **Then**
   a data de conclusão é exibida corretamente.

3. **Given** um ID de tarefa inexistente, **When** o usuário solicita seus detalhes,
   **Then** o sistema retorna uma resposta indicando que a tarefa não foi encontrada.

---

### Edge Cases

- O que acontece se o título contiver apenas espaços em branco? O sistema DEVE rejeitar.
- O que acontece se a data limite for uma data no passado? O sistema DEVE aceitar (tarefas
  retroativas podem ser legítimas), mas pode emitir aviso.
- O que acontece se o usuário tentar acessar uma tarefa de outro usuário? O sistema DEVE
  negar acesso.
- O que acontece se a data limite for igual à data de criação? O sistema DEVE aceitar.
- O que acontece se o usuário tentar excluir uma tarefa já excluída? O sistema DEVE
  retornar resposta de "não encontrada".
- O que acontece se o usuário tentar excluir uma tarefa concluída? O sistema DEVE permitir
  a exclusão — não há restrição de status para exclusão.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-000**: Frontend MUST provide a login page where users enter username and password.
- **FR-000a**: Frontend MUST call the `auth-app` login endpoint with the provided
  credentials and store the returned JWT locally upon success.
- **FR-000b**: Frontend MUST automatically attach the JWT as `Authorization: Bearer <token>`
  on every subsequent request to the `todo-app` backend.
- **FR-000c**: Frontend MUST redirect authenticated users away from the login page to the
  main application area.
- **FR-000d**: Frontend MUST display a clear error message when the `auth-app` returns
  invalid credentials, without redirecting.
- **FR-000e**: Frontend MUST validate that username and password fields are non-empty
  before calling the `auth-app` (client-side validation).
- **FR-001**: System MUST allow authenticated users to create a task; the title is
  mandatory, must be non-empty, and MUST be rejected if absent or containing only
  whitespace.
- **FR-003**: System MUST accept the following priority values only: BAIXA, MEDIA, ALTA.
  Any other value MUST be rejected.
- **FR-004**: System MUST accept the following status values only: CRIADA, ANDAMENTO,
  CONCLUIDA. Any other value MUST be rejected.
- **FR-005**: System MUST set the initial status to CRIADA when a new task is created,
  regardless of any status provided by the user.
- **FR-006**: System MUST accept an optional description (free text) for each task.
- **FR-007**: System MUST accept an optional due date for each task.
- **FR-008**: System MUST record the creation date automatically at the moment the task
  is created.
- **FR-009**: System MUST automatically record the completion date when the task status
  is changed to CONCLUIDA.
- **FR-010**: System MUST clear the completion date when the task status is changed away
  from CONCLUIDA back to an active status.
- **FR-011**: System MUST allow users to update the non-status fields (title, description,
  priority, dueDate) of a task they own via the update endpoint; status updates are
  exclusively handled by the dedicated status endpoint.
- **FR-012**: System MUST prevent users from accessing or modifying tasks that belong to
  other users.
- **FR-013**: System MUST persist all task data so it can be retrieved in subsequent
  sessions.
- **FR-014**: System MUST allow authenticated users to permanently delete a task they own.
- **FR-015**: System MUST prevent users from deleting tasks that belong to other users,
  without revealing whether the task exists.
- **FR-016**: System MUST return a "not found" response when the user attempts to delete
  a task that does not exist.

### Key Entities

- **Tarefa (Task)**: Unidade central do sistema. Atributos: identificador único, título
  (obrigatório), descrição (opcional), prioridade (BAIXA/MEDIA/ALTA, padrão MEDIA), status
  (CRIADA/ANDAMENTO/CONCLUIDA, inicial CRIADA), data limite (opcional), data de criação
  (automática), data de conclusão (automática, quando aplicável), referência ao usuário
  proprietário.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-002**: 100% dos campos da tarefa são armazenados e recuperados com exatidão,
  sem perda ou corrupção de dados.
- **SC-003**: A data de conclusão é registrada automaticamente em 100% das transições
  para status CONCLUIDA, sem intervenção manual do usuário.
- **SC-004**: Tentativas de criar tarefas com dados inválidos são rejeitadas em 100%
  dos casos com mensagem de erro clara e compreensível.
- **SC-005**: Usuários não conseguem acessar tarefas de outros usuários em nenhuma
  circunstância.
- **SC-006**: Tarefas excluídas não podem ser recuperadas ou acessadas após a exclusão
  em nenhuma circunstância.

### Post-Launch Outcome Metrics

> Estas métricas dependem de uso real em produção e não são verificáveis durante o
> desenvolvimento. Servem como referência para avaliação pós-entrega.

- **SC-000**: Usuários conseguem fazer login e acessar a área principal em menos de
  30 segundos.
- **SC-001**: Usuários conseguem criar uma tarefa completa em menos de 2 minutos.

## Assumptions

- A autenticação é delegada ao serviço separado `auth-app`. O usuário faz login via
  página do `todo-app` que chama o endpoint público do `auth-app`; o `auth-app` valida
  as credenciais e emite um JWT assinado com chave privada RSA.
- O frontend do `todo-app` armazena o JWT (localStorage ou sessionStorage) e o envia
  em todas as requisições no header `Authorization: Bearer <token>` via HTTP interceptor.
  O backend do `todo-app` valida o token em cada requisição usando a chave pública RSA
  e injeta o contexto do usuário — ele não gera tokens, apenas os valida.
- O registro de novos usuários está fora do escopo desta feature; assume-se que o usuário
  já possui conta no `auth-app`.
- Logout está fora do escopo desta feature; a sessão expira quando o JWT expira. O token
  é removido do localStorage ao receber HTTP 401 do `todo-app` (token expirado).
- Tarefas são sempre associadas ao usuário identificado pelo JWT da requisição.
- A data de conclusão é gerenciada automaticamente pelo sistema — o usuário não a
  preenche manualmente.
- Todas as transições de status são permitidas (qualquer → qualquer), sem restrições
  de fluxo obrigatório.
- A prioridade padrão, quando não informada, é "média".
- A descrição pode ter comprimento ilimitado (dentro de limites práticos do banco).
- A exclusão é permanente (hard delete); não há recurso de "lixeira" ou soft delete.
- O escopo desta feature cobre criação, atualização, exclusão e visualização de tarefas
  individuais; listagem/filtragem de tarefas está fora do escopo.
