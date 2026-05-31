# Endpoints Atuais Da API Arbor

Arquivo objetivo com:

- endpoints presentes hoje no backend
- requests reais
- responses reais
- endpoints do documento original que ainda faltam no final

Base path real da API atual: `/api`.

---

## Endpoints Presentes

## Auth

### `POST /api/auth/login`

Request:
```json
{
  "email": "user@example.com",
  "senha": "string"
}
```

Response:
```json
{
  "accessToken": "jwt",
  "refreshToken": "jwt",
  "email": "user@example.com",
  "nome": "Nome"
}
```

### `POST /api/auth/refresh`

Request:
```json
{
  "refreshToken": "jwt"
}
```

Response:
```json
{
  "accessToken": "jwt",
  "refreshToken": "jwt",
  "email": "user@example.com",
  "nome": "Nome"
}
```

### `POST /api/auth/registrar`

Request:
```json
{
  "nome": "Nome",
  "email": "user@example.com",
  "senha": "string",
  "perfilAcesso": "PUBLICO_GERAL"
}
```

Response:
```json
{
  "id": "uuid",
  "nome": "Nome",
  "email": "user@example.com",
  "perfilAcesso": "PUBLICO_GERAL",
  "ativo": true
}
```

---

## Recuperacao De Senha

### `POST /api/recuperar-senha/solicitar`

Request:
```json
{
  "email": "user@example.com"
}
```

Response:
```text
Codigo de recuperacao enviado para o e-mail informado.
```

### `POST /api/recuperar-senha/verificar`

Request:
```json
{
  "codigo": "123456"
}
```

Response:
```text
Codigo valido. Prossiga para redefinir sua senha.
```

### `POST /api/recuperar-senha/redefinir`

Request:
```json
{
  "codigo": "123456",
  "email": "user@example.com",
  "novaSenha": "Senha123",
  "confirmarSenha": "Senha123"
}
```

Response:
```text
Senha redefinida com sucesso.
```

---

## Usuarios

### `GET /api/usuarios`

Query params:
```json
{
  "ativo": true
}
```

Response:
```json
[
  {
    "id": "uuid",
    "nome": "Nome",
    "email": "email@example.com",
    "perfilAcesso": "PESQUISADOR",
    "ativo": true
  }
]
```

### `GET /api/usuarios/{id}`

Path params:
```json
{
  "id": "uuid"
}
```

Response:
```json
{
  "id": "uuid",
  "nome": "Nome",
  "email": "email@example.com",
  "perfilAcesso": "PESQUISADOR",
  "ativo": true
}
```

### `GET /api/usuarios/email`

Query params:
```json
{
  "email": "email@example.com"
}
```

Response:
```json
{
  "id": "uuid",
  "nome": "Nome",
  "email": "email@example.com",
  "perfilAcesso": "PESQUISADOR",
  "ativo": true
}
```

### `GET /api/usuarios/perfil/{perfil}`

Path params:
```json
{
  "perfil": "PESQUISADOR"
}
```

Query params:
```json
{
  "ativo": true
}
```

Response:
```json
[
  {
    "id": "uuid",
    "nome": "Nome",
    "email": "email@example.com",
    "perfilAcesso": "PESQUISADOR",
    "ativo": true
  }
]
```

### `POST /api/usuarios`

Request:
```json
{
  "nome": "Nome",
  "email": "email@example.com",
  "senha": "string",
  "perfilAcesso": "PESQUISADOR"
}
```

Response:
```json
{
  "id": "uuid",
  "nome": "Nome",
  "email": "email@example.com",
  "perfilAcesso": "PESQUISADOR",
  "ativo": true
}
```

### `DELETE /api/usuarios/{id}`

Path params:
```json
{
  "id": "uuid"
}
```

Response:
```text
204 No Content
```

---

## Arvores

### `GET /api/arvores`

Response:
```json
[
  {
    "id": "uuid",
    "especie": "Ipe-roxo",
    "bairro": "Bairro",
    "rua": "Rua",
    "referencia": "Referencia",
    "alturaAtual": 15.7,
    "dapAtual": 24.2,
    "copaAtual": 8.1,
    "estadoGeral": "...",
    "vigor": "...",
    "problemasCopa": [],
    "problemasTronco": [],
    "problemasRaiz": [],
    "estruturaTronco": "...",
    "estruturaBase": "...",
    "estruturaCopa": "...",
    "inclinacao": "...",
    "ancoragem": "...",
    "fluxoPedestre": "...",
    "fluxoAutomovel": "...",
    "tipoVia": "...",
    "alvosPotenciais": [],
    "alvosSensiveis": [],
    "conflito": {},
    "manejo": {},
    "observacoes": "string"
  }
]
```

### `GET /api/arvores/{id}`

Path params:
```json
{
  "id": "uuid"
}
```

Response:
```json
{
  "id": "uuid",
  "especie": "Ipe-roxo",
  "bairro": "Bairro",
  "rua": "Rua",
  "referencia": "Referencia",
  "alturaAtual": 15.7,
  "dapAtual": 24.2,
  "copaAtual": 8.1,
  "estadoGeral": "...",
  "vigor": "...",
  "problemasCopa": [],
  "problemasTronco": [],
  "problemasRaiz": [],
  "estruturaTronco": "...",
  "estruturaBase": "...",
  "estruturaCopa": "...",
  "inclinacao": "...",
  "ancoragem": "...",
  "fluxoPedestre": "...",
  "fluxoAutomovel": "...",
  "tipoVia": "...",
  "alvosPotenciais": [],
  "alvosSensiveis": [],
  "conflito": {},
  "manejo": {},
  "observacoes": "string"
}
```

### `GET /api/arvores/especie`

Query params:
```json
{
  "nome": "Ipe"
}
```

Response:
```json
[
  {
    "id": "uuid",
    "especie": "Ipe-roxo",
    "bairro": "Bairro",
    "rua": "Rua",
    "referencia": "Referencia",
    "alturaAtual": 15.7,
    "dapAtual": 24.2,
    "copaAtual": 8.1,
    "estadoGeral": "...",
    "vigor": "...",
    "observacoes": "string"
  }
]
```

### `POST /api/arvores`

Request:
```json
{
  "especie": "Ipe-roxo",
  "bairro": "Bairro",
  "rua": "Rua",
  "referencia": "Referencia",
  "alturaAtual": 15.7,
  "dapAtual": 24.2,
  "copaAtual": 8.1,
  "estadoGeral": "...",
  "vigor": "...",
  "problemasRaiz": [],
  "problemasCopa": [],
  "problemasTronco": [],
  "estruturaCopa": "...",
  "estruturaTronco": "...",
  "estruturaBase": "...",
  "inclinacao": "...",
  "ancoragem": "...",
  "fluxoPedestre": "...",
  "fluxoAutomovel": "...",
  "tipoVia": "...",
  "alvosPotenciais": [],
  "alvosSensiveis": [],
  "conflito": {},
  "manejo": {},
  "observacoes": "string"
}
```

Response:
```json
{
  "id": "uuid",
  "especie": "Ipe-roxo",
  "bairro": "Bairro",
  "rua": "Rua",
  "referencia": "Referencia",
  "alturaAtual": 15.7,
  "dapAtual": 24.2,
  "copaAtual": 8.1,
  "estadoGeral": "...",
  "vigor": "...",
  "observacoes": "string"
}
```

### `PUT /api/arvores/{id}`

Path params:
```json
{
  "id": "uuid"
}
```

Request:
```json
{
  "especie": "Ipe-roxo",
  "bairro": "Bairro",
  "rua": "Rua",
  "referencia": "Referencia",
  "alturaAtual": 15.7,
  "dapAtual": 24.2,
  "copaAtual": 8.1,
  "estadoGeral": "...",
  "vigor": "...",
  "problemasRaiz": [],
  "problemasCopa": [],
  "problemasTronco": [],
  "estruturaCopa": "...",
  "estruturaTronco": "...",
  "estruturaBase": "...",
  "inclinacao": "...",
  "ancoragem": "...",
  "fluxoPedestre": "...",
  "fluxoAutomovel": "...",
  "tipoVia": "...",
  "alvosPotenciais": [],
  "alvosSensiveis": [],
  "conflito": {},
  "manejo": {},
  "observacoes": "string"
}
```

Response:
```json
{
  "id": "uuid",
  "especie": "Ipe-roxo",
  "bairro": "Bairro",
  "rua": "Rua",
  "referencia": "Referencia",
  "alturaAtual": 15.7,
  "dapAtual": 24.2,
  "copaAtual": 8.1,
  "estadoGeral": "...",
  "vigor": "...",
  "observacoes": "string"
}
```

### `DELETE /api/arvores/{id}`

Path params:
```json
{
  "id": "uuid"
}
```

Response:
```text
204 No Content
```

---

## Registros

### `GET /api/registros/status/{status}`

Path params:
```json
{
  "status": "PENDENTE"
}
```

Response:
```json
[
  {
    "id": "uuid",
    "pesquisador": {},
    "dataColeta": "2026-05-26T09:00:00",
    "arvore": {},
    "administradorResponsavel": null,
    "dataAnalise": null,
    "motivoRecusa": null,
    "status": "PENDENTE",
    "alturaColetada": 15.7,
    "dapColetada": 24.2,
    "copaColetada": 8.1,
    "estadoGeral": "...",
    "vigor": "...",
    "observacoes": "string"
  }
]
```

### `GET /api/registros/pesquisador`

Response:
```json
[
  {
    "id": "uuid",
    "pesquisador": {},
    "dataColeta": "2026-05-26T09:00:00",
    "arvore": {},
    "status": "PENDENTE",
    "alturaColetada": 15.7,
    "dapColetada": 24.2,
    "copaColetada": 8.1,
    "observacoes": "string"
  }
]
```

### `GET /api/registros/pesquisador/status/{status}`

Path params:
```json
{
  "status": "PENDENTE"
}
```

Response:
```json
[
  {
    "id": "uuid",
    "pesquisador": {},
    "dataColeta": "2026-05-26T09:00:00",
    "arvore": {},
    "status": "PENDENTE",
    "alturaColetada": 15.7,
    "dapColetada": 24.2,
    "copaColetada": 8.1,
    "observacoes": "string"
  }
]
```

### `GET /api/registros/arvore/{id}`

Path params:
```json
{
  "id": "uuid"
}
```

Response:
```json
[
  {
    "id": "uuid",
    "pesquisador": {},
    "dataColeta": "2026-05-26T09:00:00",
    "arvore": {},
    "administradorResponsavel": null,
    "dataAnalise": null,
    "motivoRecusa": null,
    "status": "PENDENTE",
    "alturaColetada": 15.7,
    "dapColetada": 24.2,
    "copaColetada": 8.1,
    "estadoGeral": "...",
    "vigor": "...",
    "observacoes": "string"
  }
]
```

### `POST /api/registros`

Request:
```json
{
  "arvoreId": "uuid",
  "alturaColetada": 15.7,
  "dapColetada": 24.2,
  "copaColetada": 8.1,
  "estadoGeral": "...",
  "vigor": "...",
  "problemasCopa": [],
  "problemasTronco": [],
  "problemasRaiz": [],
  "estruturaTronco": "...",
  "estruturaBase": "...",
  "estruturaCopa": "...",
  "inclinacaoTronco": "...",
  "ancoragem": "...",
  "fluxoPedestre": "...",
  "fluxoAutomovel": "...",
  "tipoVia": "...",
  "alvosPotenciais": [],
  "alvosSensiveis": [],
  "conflito": {},
  "manejo": {},
  "observacoes": "string"
}
```

Response:
```json
{
  "id": "uuid",
  "pesquisador": {},
  "dataColeta": "2026-05-26T09:00:00",
  "arvore": {},
  "administradorResponsavel": null,
  "dataAnalise": null,
  "motivoRecusa": null,
  "status": "PENDENTE",
  "alturaColetada": 15.7,
  "dapColetada": 24.2,
  "copaColetada": 8.1,
  "observacoes": "string"
}
```

### `POST /api/registros/nova-arvore`

Request:
```json
{
  "especie": "Ipe-roxo",
  "bairro": "Bairro",
  "rua": "Rua",
  "referencia": "Referencia",
  "alturaColetada": 15.7,
  "dapColetada": 24.2,
  "copaColetada": 8.1,
  "estadoGeral": "...",
  "vigor": "...",
  "problemasCopa": [],
  "problemasTronco": [],
  "problemasRaiz": [],
  "estruturaTronco": "...",
  "estruturaBase": "...",
  "estruturaCopa": "...",
  "inclinacaoTronco": "...",
  "ancoragem": "...",
  "fluxoPedestre": "...",
  "fluxoAutomovel": "...",
  "tipoVia": "...",
  "alvosPotenciais": [],
  "alvosSensiveis": [],
  "conflito": {},
  "manejo": {},
  "observacoes": "string"
}
```

Response:
```json
{
  "especie": "Ipe-roxo",
  "bairro": "Bairro",
  "rua": "Rua",
  "referencia": "Referencia",
  "pesquisador": {},
  "dataColeta": "2026-05-26T09:00:00",
  "administradorResponsavel": null,
  "dataAnalise": null,
  "motivoRecusa": null,
  "status": "PENDENTE",
  "alturaColetada": 15.7,
  "dapColetada": 24.2,
  "copaColetada": 8.1,
  "observacoes": "string"
}
```

### `PUT /api/registros/{id}/aprovar`

Path params:
```json
{
  "id": "uuid"
}
```

Request:
```json
{}
```

Response:
```json
{
  "id": "uuid",
  "pesquisador": {},
  "dataColeta": "2026-05-26T09:00:00",
  "arvore": {},
  "administradorResponsavel": {},
  "dataAnalise": "2026-05-26T09:00:00",
  "motivoRecusa": null,
  "status": "APROVADO",
  "alturaColetada": 15.7,
  "dapColetada": 24.2,
  "copaColetada": 8.1,
  "observacoes": "string"
}
```

### `PUT /api/registros/{id}/recusar`

Path params:
```json
{
  "id": "uuid"
}
```

Request:
```json
{
  "motivoRecusa": "Medicoes inconsistentes"
}
```

Response:
```json
{
  "id": "uuid",
  "pesquisador": {},
  "dataColeta": "2026-05-26T09:00:00",
  "arvore": {},
  "administradorResponsavel": {},
  "dataAnalise": "2026-05-26T09:00:00",
  "motivoRecusa": "Medicoes inconsistentes",
  "status": "RECUSADO",
  "alturaColetada": 15.7,
  "dapColetada": 24.2,
  "copaColetada": 8.1,
  "observacoes": "string"
}
```

### `DELETE /api/registros/{id}`

Path params:
```json
{
  "id": "uuid"
}
```

Response:
```text
204 No Content
```

---

# Endpoints Do Documento Original Que Ainda Faltam

Esta lista mostra os endpoints do documento original que nao existem hoje como contrato direto na API atual.

## Auth / Sessao

### `GET /auth/me`

Objetivo: retornar usuario autenticado pelo token.

Status atual: falta endpoint direto.

Alternativa parcial: `GET /api/usuarios/{id}`, mas exige saber o `id` do usuario.

Response esperado:
```json
{
  "id": "uuid",
  "name": "Nome",
  "email": "user@example.com",
  "role": "admin"
}
```

---

## Perfil

### `GET /users/me`

Objetivo: buscar perfil do usuario logado.

Status atual: falta endpoint direto.

Alternativa parcial: `GET /api/usuarios/{id}`.

Response esperado:
```json
{
  "id": "uuid",
  "name": "Nome",
  "email": "user@example.com",
  "cpf": "00000000000",
  "role": "researcher"
}
```

### `PATCH /users/me`

Objetivo: atualizar dados pessoais do usuario logado.

Status atual: falta.

Request esperado:
```json
{
  "name": "Novo nome",
  "email": "novo@email.com",
  "cpf": "00000000000"
}
```

Response esperado:
```json
{
  "message": "Perfil atualizado",
  "user": {}
}
```

### `POST /users/me/change-password`

Objetivo: alterar senha do usuario autenticado.

Status atual: falta.

Request esperado:
```json
{
  "currentPassword": "string",
  "newPassword": "string",
  "confirmPassword": "string"
}
```

Response esperado:
```json
{
  "message": "Senha atualizada"
}
```

---

## Gestao De Usuarios

### `PATCH /users/:id`

Objetivo: atualizar role, status ou dados de usuario.

Status atual: falta.

Request esperado:
```json
{
  "name": "Nome",
  "email": "email@example.com",
  "role": "researcher",
  "active": true
}
```

Response esperado:
```json
{
  "id": "uuid",
  "name": "Nome",
  "email": "email@example.com",
  "role": "researcher",
  "active": true
}
```

---

## Dashboard Publico

### `GET /public/dashboard`

Objetivo: retornar indicadores publicos.

Status atual: falta.

Response esperado:
```json
{
  "totalTrees": 42,
  "healthyTrees": 30,
  "treesUnderMonitoring": 7,
  "removedTrees": 5
}
```

---

## Mapa

### `GET /map/trees`

Objetivo: retornar arvores para mapa por viewport.

Status atual: falta endpoint especifico.

Alternativa parcial fraca: `GET /api/arvores`, sem `bbox`, `zoom`, clusters ou payload leve.

Request esperado:
```json
{
  "bbox": {
    "minLng": -34.96,
    "minLat": -8.03,
    "maxLng": -34.92,
    "maxLat": -8.0
  },
  "zoom": 15,
  "status": "saudavel",
  "search": "ipe",
  "species": "Handroanthus impetiginosus",
  "includeCut": false,
  "limit": 300
}
```

Response esperado:
```json
{
  "items": [
    {
      "type": "tree",
      "id": "uuid",
      "codigo": "UFRPE-1001",
      "nomeComum": "Ipe-roxo",
      "especie": "Handroanthus impetiginosus",
      "lat": -8.01,
      "lng": -34.94,
      "status": "saudavel",
      "ultimaMedicao": "2026-05-01"
    }
  ],
  "meta": {
    "totalInView": 128,
    "returned": 128,
    "zoom": 15
  }
}
```

### `GET /map/trees/:treeId/detail`

Objetivo: retornar detalhe otimizado para painel do mapa.

Status atual: falta endpoint especifico.

Alternativa parcial: `GET /api/arvores/{id}`.

Response esperado:
```json
{
  "id": "uuid",
  "codigo": "UFRPE-1001",
  "nomeComum": "Ipe-roxo",
  "especie": "Handroanthus impetiginosus",
  "lat": -8.01,
  "lng": -34.94,
  "currentRecord": {
    "id": "uuid",
    "status": "saudavel",
    "alturaM": 15.7,
    "dapCm": 24.2,
    "copaM": 8.1,
    "ultimaMedicao": "2026-05-01"
  }
}
```

---

## Trees / Records

### `GET /trees/:treeId/records/:recordId`

Objetivo: buscar detalhe de um registro especifico.

Status atual: falta.

Alternativa parcial: `GET /api/registros/arvore/{id}` lista todos os registros de uma arvore, mas nao busca diretamente por `recordId`.

Response esperado:
```json
{
  "id": "uuid",
  "treeId": "uuid",
  "version": 3,
  "kind": "measurement",
  "status": "saudavel",
  "localizacao": {},
  "dimensoes": {},
  "condicao": {},
  "estruturaRisco": {},
  "conflitos": {},
  "manejo": {},
  "registro": {},
  "observacoes": "string"
}
```

### `PATCH /trees/:treeId/records/:recordId`

Objetivo: editar registro historico.

Status atual: falta.

Request esperado:
```json
{
  "treeId": "uuid",
  "recordId": "uuid",
  "localizacao": {},
  "dimensoes": {},
  "condicao": {},
  "estruturaRisco": {},
  "conflitos": {},
  "manejo": {},
  "observacoes": "string",
  "fotos": []
}
```

---

## Approval Requests

### `GET /approval-requests`

Objetivo: listar solicitacoes de aprovacao.

Status atual: falta entidade/endpoint direto.

Alternativa parcial: `GET /api/registros/status/PENDENTE`.

Request esperado:
```json
{
  "type": "create_tree",
  "status": "pendente",
  "searchField": "researcher",
  "search": "ana",
  "page": 1,
  "limit": 20
}
```

Response esperado:
```json
{
  "items": [
    {
      "id": "uuid",
      "type": "create_tree",
      "status": "pendente",
      "submittedAt": "2026-05-26T09:00:00Z",
      "submittedBy": "Nome",
      "treeId": null,
      "targetRecordId": null,
      "treeMeta": {},
      "treeDraft": {},
      "record": {}
    }
  ],
  "total": 12
}
```

### `GET /approval-requests/:id`

Objetivo: buscar detalhe de uma solicitacao de aprovacao.

Status atual: falta.

### `POST /approval-requests/tree-creation`

Objetivo: pesquisador solicitar criacao de arvore.

Status atual: falta contrato direto.

Alternativa parcial: `POST /api/registros/nova-arvore`.

Request esperado:
```json
{
  "treeDraft": {
    "nomeComum": "Ipe-roxo",
    "especie": "Handroanthus impetiginosus",
    "lat": -8.01,
    "lng": -34.94
  },
  "recordDraft": {}
}
```

### `POST /approval-requests/record-creation`

Objetivo: pesquisador solicitar nova medicao para arvore existente.

Status atual: falta contrato direto.

Alternativa parcial: `POST /api/registros`.

Request esperado:
```json
{
  "treeId": "uuid",
  "recordDraft": {}
}
```

### `POST /approval-requests/record-edit`

Objetivo: pesquisador solicitar edicao de registro.

Status atual: falta.

Request esperado:
```json
{
  "treeId": "uuid",
  "targetRecordId": "uuid",
  "recordDraft": {}
}
```

### `POST /approval-requests/:id/approve`

Objetivo: aprovar solicitacao.

Status atual: falta contrato direto.

Alternativa parcial: `PUT /api/registros/{id}/aprovar`.

### `POST /approval-requests/:id/reject`

Objetivo: rejeitar solicitacao com motivo.

Status atual: falta contrato direto.

Alternativa parcial: `PUT /api/registros/{id}/recusar`, usando `motivoRecusa`.

---

## Dashboards Por Papel

### `GET /dashboards/researcher`
Status atual: falta.

### `GET /dashboards/admin`
Status atual: falta.

### `GET /dashboards/manager`
Status atual: falta.

### `GET /dashboards/citizen`
Status atual: falta.

Response esperado:
```json
{
  "stats": [],
  "recentActivity": [],
  "pendingCount": 3
}
```

---

## Resumo Dos Principais Faltantes

| Dominio | Faltando |
|---|---|
| Auth | endpoint direto de usuario autenticado: `/auth/me` |
| Perfil | buscar perfil por `me`, atualizar perfil e trocar senha autenticado |
| Usuarios | update parcial de usuario |
| Dashboard | dashboard publico e dashboards por papel |
| Mapa | endpoint espacial com `bbox`, `zoom`, clusters e detalhe otimizado |
| Records | detalhe por `recordId` e edicao de registro historico |
| Approval Requests | entidade/endpoints dedicados de solicitacao de aprovacao |
