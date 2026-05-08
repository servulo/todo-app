# Quickstart: Criação e Gerenciamento de Tarefas

**Feature**: 001-task-management
**Date**: 2026-05-06

## Pré-requisitos

- Java (última versão LTS estável)
- Maven 3.9+
- Node.js 20 LTS + npm
- Docker Desktop (para SQL Server em dev/testes)
- Acesso ao `auth-app` rodando localmente ou em URL configurável

---

## 1. Backend (Quarkus)

### 1.1 Configurar variáveis de ambiente

```properties
# backend/src/main/resources/application.properties (ou via env vars)

# DataSource SQL Server
quarkus.datasource.db-kind=mssql
quarkus.datasource.username=${DB_USER:sa}
quarkus.datasource.password=${DB_PASSWORD:YourStrong@Passw0rd}
quarkus.datasource.jdbc.url=jdbc:sqlserver://${DB_HOST:localhost}:${DB_PORT:1433};databaseName=${DB_NAME:tododb};encrypt=false

# Hibernate
quarkus.hibernate-orm.database.generation=update

# JWT — chave pública RSA do auth-app
mp.jwt.verify.publickey.location=${JWT_PUBLIC_KEY_LOCATION:publickey.pem}
mp.jwt.verify.issuer=${JWT_ISSUER:http://auth-app}

# Logging JSON
quarkus.log.console.json=true
```

### 1.2 Subir SQL Server via Docker

```bash
docker run -e ACCEPT_EULA=Y \
           -e SA_PASSWORD=YourStrong@Passw0rd \
           -p 1433:1433 \
           --name sqlserver-dev \
           -d mcr.microsoft.com/mssql/server:2022-latest
```

### 1.3 Rodar o backend em modo dev

```bash
cd backend
mvn quarkus:dev
```

API disponível em `http://localhost:8080`
OpenAPI UI em `http://localhost:8080/q/swagger-ui`
Health check em `http://localhost:8080/q/health`

---

## 2. Frontend (Angular)

### 2.1 Instalar dependências

```bash
cd frontend
npm install
```

### 2.2 Configurar ambiente

```typescript
// frontend/src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',          // todo-app backend
  authApiUrl: 'http://localhost:8081/api/auth'  // auth-app backend
};
```

### 2.3 Rodar o frontend

```bash
cd frontend
npm start
```

App disponível em `http://localhost:4200`

---

## 3. Rodar os Testes

### 3.1 Testes de integração do backend (requer Docker)

```bash
cd backend
mvn verify
```

Os testes de integração sobem automaticamente um container SQL Server via
Testcontainers. Docker deve estar rodando.

### 3.2 Testes unitários do frontend

```bash
cd frontend
npm test
```

---

## 4. Fluxo Manual de Validação

### 4.1 Obter JWT do auth-app

```bash
# Exemplo com curl — substitua pela URL real do auth-app
curl -X POST http://localhost:8081/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"user@example.com","password":"senha123"}'
# Resposta: {"token": "eyJ..."}
```

### 4.2 Criar uma tarefa (US1)

```bash
TOKEN="eyJ..."

curl -X POST http://localhost:8080/api/tasks \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "title": "Estudar Quarkus",
       "priority": "ALTA",
       "dueDate": "2026-06-01"
     }'
# Resposta 201: {"id":"...","status":"CRIADA",...}
```

### 4.3 Atualizar status para CONCLUIDA (US2)

```bash
TASK_ID="<id retornado acima>"

curl -X PATCH http://localhost:8080/api/tasks/$TASK_ID/status \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"status": "CONCLUIDA"}'
# Resposta 200: {"completedAt":"2026-05-06T...","status":"CONCLUIDA"}
```

### 4.4 Excluir a tarefa (US3)

```bash
curl -X DELETE http://localhost:8080/api/tasks/$TASK_ID \
     -H "Authorization: Bearer $TOKEN"
# Resposta 204 No Content
```

### 4.5 Verificar que a tarefa não existe mais (US3 / US4)

```bash
curl http://localhost:8080/api/tasks/$TASK_ID \
     -H "Authorization: Bearer $TOKEN"
# Resposta 404: {"message":"Task not found"}
```

---

## 5. Checklist de Validação (Golden Path)

### Frontend — Login (US1)
- [ ] Página de login acessível em `http://localhost:4200/login`
- [ ] Submeter formulário com credenciais válidas armazena JWT e redireciona
- [ ] Submeter com credenciais inválidas exibe mensagem de erro
- [ ] Submeter com campos em branco exibe validação local (sem chamada ao auth-app)
- [ ] Usuário autenticado redirecionado ao acessar `/login`

### Backend — Tarefas (US2–US5)
- [ ] Backend inicia sem erros em modo dev
- [ ] Health check `/q/health` retorna `UP`
- [ ] OpenAPI UI acessível em `/q/swagger-ui`
- [ ] POST /api/tasks cria tarefa com status `CRIADA`
- [ ] POST /api/tasks sem título retorna 400
- [ ] POST /api/tasks sem token retorna 401
- [ ] PATCH /api/tasks/{id}/status → `CONCLUIDA` preenche `completedAt`
- [ ] PATCH /api/tasks/{id}/status → `ANDAMENTO` zera `completedAt`
- [ ] DELETE /api/tasks/{id} retorna 204
- [ ] GET /api/tasks/{id} após DELETE retorna 404
- [ ] GET /api/tasks/{id} de outro usuário retorna 404
