# API Contract: Auth — Login

**Service**: auth-app (serviço externo — não faz parte deste repositório)
**Consumidor**: frontend do `todo-app` (página de login)
**Base path**: configurável via `environment.authApiUrl`
**Content-Type**: `application/json`
**Date**: 2026-05-06

> Este contrato documenta o endpoint do `auth-app` **consumido** pelo frontend do
> `todo-app`. O `todo-app` não implementa este endpoint — ele apenas o chama.

---

## POST /api/auth/login — Autenticar Usuário

**User Story**: US1 (P1) — Fazer Login

### Request

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "usuario123",
  "password": "senhaSegura123"
}
```

| Campo      | Tipo   | Obrigatório | Validações (client-side) |
|------------|--------|-------------|--------------------------|
| `username` | string | Sim         | Não vazio                |
| `password` | string | Sim         | Não vazio                |

### Responses

**200 OK** — credenciais válidas
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

O JWT retornado:
- É assinado com chave privada RSA pelo `auth-app`
- Contém o claim `sub` com o identificador único do usuário
- Contém o claim `exp` com a data de expiração

**401 Unauthorized** — credenciais inválidas
```json
{
  "message": "Invalid credentials"
}
```

**400 Bad Request** — payload inválido (campos ausentes/malformados)
```json
{
  "message": "Bad request"
}
```

---

## Comportamento esperado no frontend (AuthService)

```
1. Usuário submete formulário com username + password
2. AuthService chama POST /api/auth/login
3. Em caso de 200: armazena token (localStorage) → redireciona para /tasks
4. Em caso de 401: exibe "Credenciais inválidas"
5. Em caso de erro de rede: exibe "Serviço indisponível, tente novamente"
```

## Armazenamento do JWT

- **Onde**: `localStorage` sob a chave `auth_token`
- **Quando remover**: ao fazer logout (fora do escopo desta feature) ou ao receber
  HTTP 401 do `todo-app` (token expirado)

## Configuração de ambiente (frontend)

```typescript
// frontend/src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',          // todo-app backend
  authApiUrl: 'http://localhost:8081/api/auth'  // auth-app backend
};
```
