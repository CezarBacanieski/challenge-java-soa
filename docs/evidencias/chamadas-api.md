# Evidências: chamadas reais à API

Gerado em 25/09/2026 11:05 com a API rodando localmente (`./mvnw spring-boot:run` + PostgreSQL no Docker).
Cada bloco mostra a requisição enviada e a resposta **exatamente** como a API devolveu. Os tokens foram encurtados.

## 1. Autenticação (JWT)

### Login com sucesso (endpoint público) → 200 + token

```http
POST /api/auth/login
Content-Type: application/json

{"email": "admin@ford.com", "senha": "Admin@123"}
```

Resposta: **200 OK**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...(token completo omitido)",
  "tipo": "Bearer",
  "expiraEmSegundos": 3600,
  "email": "admin@ford.com",
  "perfil": "ADMIN",
  "concessionariaId": null
}
```

### Login com senha errada → 401

```http
POST /api/auth/login
Content-Type: application/json

{"email": "admin@ford.com", "senha": "SenhaErrada@1"}
```

Resposta: **401 Unauthorized**

```json
{
  "status": 401,
  "erro": "E-mail ou senha inválidos",
  "timestamp": "2026-09-25T11:05:40.734949",
  "path": "/api/auth/login"
}
```

### Usuário do token → 200 (sem expor a senha)

```http
GET /api/auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...(token)
```

Resposta: **200 OK**

```json
{
  "id": 2,
  "nome": "Gestor Centro SP",
  "email": "gestor@ford.com",
  "perfil": "GESTOR",
  "concessionariaId": 1,
  "ativo": true
}
```

## 2. Acesso não autenticado (401)

### Rota protegida sem token → 401

```http
GET /api/clientes
```

Resposta: **401 Unauthorized**

```json
{
  "status": 401,
  "erro": "Autenticação necessária: envie um token JWT válido",
  "timestamp": "2026-09-25T11:05:40.825207",
  "path": "/api/clientes"
}
```

### Token adulterado → 401

```http
GET /api/clientes
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...(token)
```

Resposta: **401 Unauthorized**

```json
{
  "status": 401,
  "erro": "Token inválido",
  "timestamp": "2026-09-25T11:05:40.866924",
  "path": "/api/clientes"
}
```

## 3. Autorização por perfil (403)

### CONSULTOR tenta abrir o dashboard → 403

```http
GET /api/dashboard/resumo
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...(token)
```

Resposta: **403 Forbidden**

```json
{
  "status": 403,
  "erro": "Acesso negado: seu perfil não tem permissão para este recurso",
  "timestamp": "2026-09-25T11:05:40.909194",
  "path": "/api/dashboard/resumo"
}
```

### CONSULTOR tenta excluir um cliente → 403

```http
DELETE /api/clientes/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...(token)
```

Resposta: **403 Forbidden**

```json
{
  "status": 403,
  "erro": "Acesso negado: seu perfil não tem permissão para este recurso",
  "timestamp": "2026-09-25T11:05:40.949669",
  "path": "/api/clientes/1"
}
```

### GESTOR de SP consulta indicadores da própria concessionária → 200

```http
GET /api/concessionarias/1/indicadores
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...(token)
```

Resposta: **200 OK**

```json
{
  "concessionariaId": 1,
  "concessionariaNome": "Ford Centro SP",
  "totalManutencoes": 0,
  "veiculosAtendidos": 0,
  "veiculosRetidos": 0,
  "taxaRetencao": 0.0
}
```

### GESTOR de SP tenta ver indicadores de Campinas → 403 (regra pelo claim concessionariaId)

```http
GET /api/concessionarias/2/indicadores
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...(token)
```

Resposta: **403 Forbidden**

```json
{
  "status": 403,
  "erro": "Acesso negado: você só pode consultar indicadores da sua concessionária",
  "timestamp": "2026-09-25T11:05:41.045269",
  "path": "/api/concessionarias/2/indicadores"
}
```

## 4. REST nível 2: métodos e status codes

### Criar cliente → 201 + Location

```http
POST /api/clientes
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...(token)
Content-Type: application/json

{"nome": "Maria Silva", "email": "maria756131557@email.com", "telefone": "11988887777"}
```

Resposta: **201 Created**
(`Location: http://localhost:8080/api/clientes/1`)

```json
{
  "id": 1,
  "nome": "Maria Silva",
  "email": "maria756131557@email.com",
  "telefone": "11988887777",
  "dataCadastro": "2026-09-25"
}
```

### Criar cliente com e-mail repetido → 409

```http
POST /api/clientes
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...(token)
Content-Type: application/json

{"nome": "Maria Silva", "email": "maria756131557@email.com", "telefone": "11988887777"}
```

Resposta: **409 Conflict**

```json
{
  "status": 409,
  "erro": "Já existe um cliente com este e-mail",
  "timestamp": "2026-09-25T11:05:41.159601",
  "path": "/api/clientes"
}
```

### Criar veículo com VIN inválido → 400 com lista de campos

```http
POST /api/veiculos
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...(token)
Content-Type: application/json

{"vin": "123", "marca": "Ford", "modelo": "Ranger", "anoFabricacao": 2024, "clienteId": 1}
```

Resposta: **400 Bad Request**

```json
{
  "status": 400,
  "erro": "Campos inválidos",
  "timestamp": "2026-09-25T11:05:41.210172",
  "path": "/api/veiculos",
  "campos": [
    {
      "campo": "vin",
      "mensagem": "VIN deve ter 17 caracteres alfanuméricos maiúsculos (sem I, O e Q)"
    }
  ]
}
```

### Buscar recurso inexistente → 404

```http
GET /api/clientes/999999
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...(token)
```

Resposta: **404 Not Found**

```json
{
  "status": 404,
  "erro": "Cliente não encontrado",
  "timestamp": "2026-09-25T11:05:41.255265",
  "path": "/api/clientes/999999"
}
```

### Listar leads filtrando por status → 200

```http
GET /api/leads?status=PENDENTE
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...(token)
```

Resposta: **200 OK**

```json
[]
```

### Método não suportado → 405

```http
PATCH /api/clientes
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...(token)
```

Resposta: **405 Method Not Allowed**

```json
{
  "status": 405,
  "erro": "Método HTTP não suportado para este recurso",
  "timestamp": "2026-09-25T11:05:41.340735",
  "path": "/api/clientes"
}
```

