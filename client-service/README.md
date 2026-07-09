# Client Service

Microsserviço responsável pelo cadastro, consulta e manutenção de clientes no projeto `spring-cloud-microservices`.

Ele centraliza as regras de negócio de clientes para que outros serviços, como o `order-service`, possam validar se um cliente existe e se está ativo antes de executar suas próprias operações.

## Tecnologias

- Java 17
- Spring Boot 3.2.1
- Spring Web
- Spring Data JPA
- Bean Validation
- Spring Cloud Netflix Eureka Client
- PostgreSQL em Docker
- H2 para execução local/testes
- Springdoc OpenAPI/Swagger
- Maven

## Responsabilidades

- Criar clientes.
- Buscar cliente por ID.
- Listar clientes com paginação e filtros.
- Atualizar cliente totalmente com `PUT`.
- Atualizar cliente parcialmente com `PATCH`.
- Ativar e inativar clientes.
- Inativar cliente via `DELETE` lógico, sem apagar o registro do banco.
- Bloquear email duplicado.
- Bloquear documento duplicado.
- Retornar erros no formato padronizado do projeto.

## Porta

```text
8081
```

## Execução Local

Na raiz do `client-service`:

```bash
mvn spring-boot:run
```

Por padrão, o serviço local usa H2 em memória e não tenta se registrar no Eureka.

URLs úteis:

```text
API: http://localhost:8081
Swagger: http://localhost:8081/swagger-ui/index.html
OpenAPI JSON: http://localhost:8081/v3/api-docs
H2 Console: http://localhost:8081/h2-console
Health: http://localhost:8081/actuator/health
```

## Execução Com Docker

Na raiz do projeto:

```bash
docker compose up --build
```

No profile `docker`, o serviço usa PostgreSQL e se registra no Eureka.

## Header Obrigatório

As rotas privadas exigem o header `X-User-Id`.

Exemplo:

```http
X-User-Id: local-user
```

Esse header representa o usuário que executou a operação. Atualmente ele é usado para rastreabilidade em logs e para manter o contrato entre o API Gateway e os microsserviços.

A única rota pública deste serviço é:

```text
GET /clients/public/test
```

## Endpoints

| Método | Endpoint | Descrição | Header `X-User-Id` |
| --- | --- | --- | --- |
| `GET` | `/clients/public/test` | Teste público do serviço | Não |
| `POST` | `/clients` | Cria um cliente | Sim |
| `GET` | `/clients/{id}` | Busca cliente por ID | Sim |
| `GET` | `/clients` | Lista clientes com paginação e filtros | Sim |
| `PUT` | `/clients/{id}` | Atualiza todos os dados do cliente | Sim |
| `PATCH` | `/clients/{id}` | Atualiza parcialmente o cliente | Sim |
| `PATCH` | `/clients/{id}/activate` | Ativa um cliente inativo | Sim |
| `PATCH` | `/clients/{id}/deactivate` | Inativa um cliente ativo | Sim |
| `DELETE` | `/clients/{id}` | Inativa o cliente logicamente | Sim |

## Modelo de Cliente

Resposta padrão de cliente:

```json
{
  "id": 1,
  "name": "Maria Silva",
  "document": "12345678900",
  "email": "maria.silva@email.com",
  "phone": "11999999999",
  "active": true,
  "createdAt": "2026-07-09T10:30:00",
  "updatedAt": "2026-07-09T10:35:00"
}
```

Campos:

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | `number` | Identificador do cliente |
| `name` | `string` | Nome do cliente |
| `document` | `string` | CPF com 11 dígitos ou CNPJ com 14 dígitos |
| `email` | `string` | Email do cliente |
| `phone` | `string` | Telefone do cliente |
| `active` | `boolean` | Indica se o cliente está ativo |
| `createdAt` | `datetime` | Data de criação |
| `updatedAt` | `datetime` | Data da última alteração |

## Criar Cliente

```http
POST /clients
X-User-Id: local-user
Content-Type: application/json
```

Request:

```json
{
  "name": "Maria Silva",
  "document": "12345678900",
  "email": "maria.silva@email.com",
  "phone": "11999999999"
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "name": "Maria Silva",
  "document": "12345678900",
  "email": "maria.silva@email.com",
  "phone": "11999999999",
  "active": true,
  "createdAt": "2026-07-09T10:30:00",
  "updatedAt": null
}
```

Regras:

- `name` é obrigatório e deve ter no máximo 150 caracteres.
- `document` é obrigatório, deve ser único e deve ter no máximo 20 caracteres.
- `email` é obrigatório, deve ser válido, único e deve ter no máximo 150 caracteres.
- `phone` é opcional e deve ter no máximo 20 caracteres.
- Todo cliente novo nasce com `active = true`.

## Buscar Cliente Por ID

```http
GET /clients/1
X-User-Id: local-user
```

Response `200 OK`:

```json
{
  "id": 1,
  "name": "Maria Silva",
  "document": "12345678900",
  "email": "maria.silva@email.com",
  "phone": "11999999999",
  "active": true,
  "createdAt": "2026-07-09T10:30:00",
  "updatedAt": null
}
```

Se o cliente não existir, a API retorna `404 Not Found`.

## Listar Clientes

```http
GET /clients?page=0&size=10
X-User-Id: local-user
```

Response `200 OK`:

```json
{
  "content": [
    {
      "id": 1,
      "name": "Maria Silva",
      "document": "12345678900",
      "email": "maria.silva@email.com",
      "phone": "11999999999",
      "active": true,
      "createdAt": "2026-07-09T10:30:00",
      "updatedAt": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "size": 10,
  "number": 0,
  "numberOfElements": 1,
  "empty": false
}
```

### Filtros Disponíveis

Os filtros podem ser usados separadamente ou combinados com paginação.

| Filtro | Tipo | Comportamento |
| --- | --- | --- |
| `name` | `string` | Busca parcial, sem diferenciar maiúsculas e minúsculas |
| `email` | `string` | Busca exata |
| `document` | `string` | Busca exata |
| `active` | `boolean` | Filtra clientes ativos ou inativos |
| `page` | `number` | Página iniciando em `0` |
| `size` | `number` | Quantidade de registros por página |
| `sort` | `string` | Ordenação padrão do Spring, por exemplo `name,asc` |

Exemplo:

```http
GET /clients?name=maria&email=maria.silva@email.com&document=12345678900&active=true&page=0&size=10&sort=name,asc
X-User-Id: local-user
```

## Atualizar Cliente Com PUT

```http
PUT /clients/1
X-User-Id: local-user
Content-Type: application/json
```

Request:

```json
{
  "name": "Maria Souza",
  "document": "12345678900",
  "email": "maria.souza@email.com",
  "phone": "11888888888"
}
```

Response `200 OK`:

```json
{
  "id": 1,
  "name": "Maria Souza",
  "document": "12345678900",
  "email": "maria.souza@email.com",
  "phone": "11888888888",
  "active": true,
  "createdAt": "2026-07-09T10:30:00",
  "updatedAt": "2026-07-09T10:40:00"
}
```

Regras:

- `PUT` substitui os dados editáveis do cliente.
- Os campos obrigatórios continuam sendo exigidos.
- Email e documento não podem pertencer a outro cliente.
- O campo `active` não é alterado por este endpoint.

## Atualizar Cliente Com PATCH

```http
PATCH /clients/1
X-User-Id: local-user
Content-Type: application/json
```

Request:

```json
{
  "email": "maria.novo@email.com",
  "phone": "11777777777"
}
```

Response `200 OK`:

```json
{
  "id": 1,
  "name": "Maria Silva",
  "document": "12345678900",
  "email": "maria.novo@email.com",
  "phone": "11777777777",
  "active": true,
  "createdAt": "2026-07-09T10:30:00",
  "updatedAt": "2026-07-09T10:45:00"
}
```

Regras:

- Campos não enviados são ignorados.
- `name`, `email`, `document` e `phone` não podem ser strings vazias quando enviados.
- `email`, quando enviado, deve ter formato válido e não pode estar cadastrado para outro cliente.
- `document`, quando enviado, deve conter 11 ou 14 dígitos e não pode estar cadastrado para outro cliente.
- `phone`, quando enviado, deve ter no máximo 20 caracteres.

## Ativar Cliente

```http
PATCH /clients/1/activate
X-User-Id: local-user
```

Response `200 OK`:

```json
{
  "id": 1,
  "name": "Maria Silva",
  "document": "12345678900",
  "email": "maria.silva@email.com",
  "phone": "11999999999",
  "active": true,
  "createdAt": "2026-07-09T10:30:00",
  "updatedAt": "2026-07-09T10:50:00"
}
```

Se o cliente já estiver ativo, a API retorna `409 Conflict`.

## Inativar Cliente

```http
PATCH /clients/1/deactivate
X-User-Id: local-user
```

Response `200 OK`:

```json
{
  "id": 1,
  "name": "Maria Silva",
  "document": "12345678900",
  "email": "maria.silva@email.com",
  "phone": "11999999999",
  "active": false,
  "createdAt": "2026-07-09T10:30:00",
  "updatedAt": "2026-07-09T10:55:00"
}
```

Se o cliente já estiver inativo, a API retorna `409 Conflict`.

## Delete Lógico

```http
DELETE /clients/1
X-User-Id: local-user
```

Response:

```text
204 No Content
```

Regras:

- O registro não é removido fisicamente do banco.
- O cliente permanece salvo com `active = false`.
- Se o cliente já estiver inativo, a operação continua idempotente e retorna `204 No Content`.
- Se o cliente não existir, a API retorna `404 Not Found`.

## Rota Pública de Teste

```http
GET /clients/public/test
```

Response `200 OK`:

```text
Client API public endpoint OK
```

## Erros

Os erros seguem o formato padronizado do projeto:

```json
{
  "timestamp": "2026-07-09T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação",
  "path": "/clients",
  "correlationId": "7b0f6a2c-41f2-4b2c-8b79-b9d5e88ef111",
  "errors": [
    {
      "field": "email",
      "message": "Email inválido"
    }
  ]
}
```

### Principais Status

| Status | Quando acontece |
| --- | --- |
| `400 Bad Request` | Dados inválidos, JSON inválido ou header obrigatório ausente |
| `404 Not Found` | Cliente não encontrado |
| `409 Conflict` | Email/documento duplicado ou tentativa de ativar/inativar sem necessidade |
| `500 Internal Server Error` | Erro inesperado |

### Exemplos de Erro

Header `X-User-Id` ausente:

```json
{
  "timestamp": "2026-07-09T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Header obrigatório ausente: X-User-Id",
  "path": "/clients",
  "correlationId": "7b0f6a2c-41f2-4b2c-8b79-b9d5e88ef111"
}
```

Cliente não encontrado:

```json
{
  "timestamp": "2026-07-09T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Cliente não encontrado",
  "path": "/clients/99",
  "correlationId": "7b0f6a2c-41f2-4b2c-8b79-b9d5e88ef111"
}
```

Email duplicado:

```json
{
  "timestamp": "2026-07-09T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Email já cadastrado",
  "path": "/clients",
  "correlationId": "7b0f6a2c-41f2-4b2c-8b79-b9d5e88ef111"
}
```

## Correlation ID

O serviço lê o header `X-Correlation-Id` quando ele é enviado. Se não for enviado, a API gera um UUID automaticamente nas respostas de erro.

Exemplo:

```http
X-Correlation-Id: demo-correlation-id
```

## Comandos Úteis

Rodar testes:

```bash
mvn clean test
```

Gerar pacote local:

```bash
mvn clean package
```

Build Docker apenas do `client-service`:

```bash
docker compose build client-api
```

Subir o projeto completo:

```bash
docker compose up --build
```
