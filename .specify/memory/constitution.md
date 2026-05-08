<!--
SYNC IMPACT REPORT
==================
Version change: 1.0.3 → 1.1.0
Modified principles: N/A
Modified stack constraints: N/A
Added sections:
  - Restrições de Stack › Autenticação (novo bloco)
Removed sections: N/A
Templates requiring updates:
  ✅ .specify/templates/plan-template.md — sem referência a autenticação; sem alterações necessárias
  ✅ .specify/templates/spec-template.md — sem impacto estrutural
  ✅ .specify/templates/tasks-template.md — sem impacto
  ✅ .specify/templates/commands/ — diretório não encontrado; nenhum arquivo de comando para atualizar
Follow-up TODOs: Nenhum placeholder deferido.
-->

# Todo App — Constituição

## Princípios Fundamentais

### I. API-First

O backend Quarkus DEVE expor toda a funcionalidade do sistema por meio de uma API REST
documentada com OpenAPI/Swagger. Nenhum recurso pode ser considerado entregue sem
contrato de API definido e acessível. O contrato de API é a fonte de verdade para a
integração entre frontend e backend.

**Regras não negociáveis**:
- Todos os endpoints DEVEM ser documentados via anotações MicroProfile OpenAPI.
- Alterações de contrato (breaking changes) DEVEM incrementar a versão MAJOR da API.
- A especificação OpenAPI gerada DEVE ser versionada junto ao código-fonte.

### II. Separação de Responsabilidades

O frontend Angular é responsável exclusivamente pela interface do usuário e pela
orquestração de chamadas à API. Lógica de negócio, validação de domínio e persistência
DEVEM residir no backend Quarkus.

**Regras não negociáveis**:
- O Angular NÃO DEVE conter regras de negócio que dupliquem validações do backend.
- Serviços Angular DEVEM se comunicar com o backend APENAS por meio de chamadas HTTP
  à API REST documentada.
- O backend Quarkus DEVE ser autossuficiente: todos os casos de uso DEVEM ser testáveis
  de forma independente do frontend.

### III. Test-First (NÃO NEGOCIÁVEL)

TDD é obrigatório em todo o desenvolvimento. O fluxo DEVE seguir:
testes escritos → aprovados pelo usuário → testes falham (red) → implementação (green)
→ refatoração. Nenhuma funcionalidade pode ser implementada sem testes escritos
previamente.

**Regras não negociáveis**:
- Testes de contrato DEVEM ser escritos antes da implementação dos endpoints.
- Testes de integração DEVEM cobrir os fluxos críticos de cada User Story.
- O ciclo Red-Green-Refactor DEVE ser respeitado; commits de implementação sem
  cobertura de testes prévia NÃO são permitidos.

### IV. Observabilidade

O sistema DEVE fornecer visibilidade suficiente para diagnóstico em produção sem
necessidade de acesso direto ao banco de dados ou ao servidor.

**Regras não negociáveis**:
- Logs DEVEM ser estruturados (JSON) e incluir nível, timestamp, correlationId e
  contexto da operação.
- Health checks DEVEM ser implementados via Quarkus SmallRye Health (`/q/health`),
  cobrindo liveness e readiness.
- Erros não tratados DEVEM ser registrados com stack trace e contexto suficiente para
  reprodução.

### V. Simplicidade

A solução mais simples que atende ao requisito é a solução correta. Abstrações,
padrões e camadas adicionais DEVEM ser justificados explicitamente no plano de
implementação.

**Regras não negociáveis**:
- Aplicar YAGNI: não implementar funcionalidades antecipando necessidades futuras não
  confirmadas.
- Abstrações que não reduzem duplicação real ou não melhoram testabilidade NÃO DEVEM
  ser introduzidas.
- Toda complexidade adicionada DEVE ser registrada na tabela de Complexity Tracking
  do plano.

## Restrições de Stack

Esta seção define as tecnologias obrigatórias. Substituições DEVEM passar pelo processo
de emenda desta constituição.

**Backend**:
- Linguagem: Java (última versão LTS estável)
- Framework: Quarkus (última versão LTS estável)
- Persistência: Hibernate ORM com Panache; SQL Server em produção e em testes
  (Docker Container via Testcontainers ou serviço Docker Compose dedicado)
- API: RESTEasy Reactive com MicroProfile OpenAPI
- Testes: JUnit 5, RestAssured, Quarkus Test Framework

**Frontend**:
- Framework: Angular (última versão LTS estável)
- Linguagem: TypeScript (strict mode habilitado)
- Testes: Jest para unitários, Cypress para E2E (quando requisitado)
- Gerenciador de pacotes: npm

**Autenticação**:
- Padrão: JWT (JSON Web Token) stateless com assinatura RSA.
- O serviço `auth-app` é responsável exclusivo por registro, login e emissão de tokens.
  Ele assina os JWTs com chave privada RSA.
- O serviço `todo-app` NUNCA emite tokens. Ele valida cada requisição usando a chave
  pública RSA e injeta o contexto do usuário nos handlers.
- O frontend do `todo-app` armazena o JWT após autenticação e o envia em todas as
  requisições protegidas via header `Authorization: Bearer <token>`.
- Endpoints de registro e login do `auth-app` são públicos (sem autenticação). Todos
  os demais endpoints do `todo-app` DEVEM exigir JWT válido.
- JWTs expirados ou com assinatura inválida DEVEM ser rejeitados com HTTP 401.

**Infraestrutura**:
- Containerização: Docker (imagem Quarkus nativa opcional)
- Variáveis de ambiente DEVEM ser a única forma de configuração entre ambientes

## Fluxo de Desenvolvimento

Todo desenvolvimento de feature DEVE seguir o fluxo Speckit:

1. **Especificação** (`/speckit-specify`): User Stories com critérios de aceitação
   mensuráveis e testáveis definidos antes de qualquer código.
2. **Planejamento** (`/speckit-plan`): Contrato de API, modelo de dados e estrutura de
   código definidos antes da implementação.
3. **Tarefas** (`/speckit-tasks`): Tarefas atômicas organizadas por User Story,
   ordenadas para entrega incremental de valor.
4. **Implementação**: Test-First obrigatório. Cada User Story entregável e testável
   de forma independente.

**Revisão de código**:
- Toda alteração DEVE passar por revisão antes do merge para a branch principal.
- PRs DEVEM referenciar a User Story correspondente e demonstrar que os testes passam.

## Governança

Esta constituição é a autoridade máxima sobre as práticas de desenvolvimento deste
projeto. Em caso de conflito entre esta constituição e qualquer outra documentação,
esta constituição prevalece.

**Processo de emenda**:
1. Proposta documentada descrevendo a mudança, motivação e impacto nos princípios.
2. Aprovação explícita do responsável pelo projeto.
3. Atualização desta constituição com incremento de versão conforme política abaixo.
4. Propagação das alterações para os templates afetados.

**Política de versionamento**:
- MAJOR: remoção ou redefinição incompatível de princípios ou seções.
- MINOR: adição de novo princípio, seção ou expansão material de orientação existente.
- PATCH: esclarecimentos, correções de redação, refinamentos sem impacto semântico.

**Conformidade**:
- Todo PR DEVE ser verificado contra os princípios desta constituição antes do merge.
- O Constitution Check no plano de implementação (`plan.md`) é o ponto formal de
  verificação de conformidade por feature.

**Versão**: 1.1.0 | **Ratificada**: 2026-05-06 | **Última alteração**: 2026-05-06
