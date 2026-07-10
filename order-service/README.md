# Order Service

Microsserviço responsável por criar, consultar e controlar o ciclo de vida de pedidos.

Este serviço faz parte do projeto [`spring-cloud-microservices`](../README.md), uma arquitetura de microsserviços com Spring Boot, Spring Cloud, Eureka, OpenFeign, API Gateway, PostgreSQL e Docker.

## Objetivo

O `order-service` centraliza as regras de pedidos da aplicação. Antes de criar um pedido, ele valida se o cliente existe e está ativo no `client-service`. Essa integração simula um cenário corporativo comum: um serviço de pedidos depende de um cadastro de clientes, mas mantém sua própria base de dados e suas próprias regras de negócio.

## Responsabilidades

- Criar pedidos apenas para clientes válidos e ativos.
- Consultar clientes no `client-service` usando OpenFeign.
- Enviar o header interno `X-User-Id: order-service` nas chamadas ao `client-service`.
- Listar pedidos com paginação.
- Buscar pedido por ID.
- Controlar transições de status do pedido.
- Bloquear alterações em pedidos finalizados ou cancelados.
- Retornar erros padronizados com `correlationId`.
- Expor documentação interativa via Swagger/OpenAPI.

## Tecnologias

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Cloud OpenFeign
- Spring Cloud Netflix Eureka Client
- Springdoc OpenAPI
- H2 para execução local simples
- PostgreSQL no Docker
- Maven
- Docker e Docker Compose

## Porta

```text
8083
```

## Endpoints

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `GET` | `/orders/public/test` | Endpoint público de teste |
| `POST` | `/orders` | Cria um pedido |
| `GET` | `/orders/{id}` | Busca um pedido por ID |
| `GET` | `/orders?page=0&size=10` | Lista pedidos com paginação |
| `PATCH` | `/orders/{id}/status?status=PROCESSING` | Atualiza o status do pedido |

## Modelo de pedido

### Request

```json
{
  "clientId": 10,
  "description": "Compra de notebook",
  "amount": 3500.00
}
```

### Response

```json
{
  "id": 1,
  "clientId": 10,
  "description": "Compra de notebook",
  "amount": 3500.00,
  "status": "CREATED",
  "createdAt": "2026-07-09T16:30:00",
  "updatedAt": "2026-07-09T16:30:00"
}
```

## Fluxo de criação de pedido

1. A API recebe `POST /orders`.
2. O payload é validado.
3. O `order-service` consulta o `client-service` via Feign em `GET /clients/{id}`.
4. A chamada interna envia `X-User-Id: order-service`.
5. Se o cliente existir e estiver ativo, o pedido é salvo com status inicial `CREATED`.
6. Se o cliente não existir, a API retorna `404 Not Found`.
7. Se o cliente estiver inativo, a API retorna `400 Bad Request`.
8. Se o `client-service` estiver indisponível, a API retorna `503 Service Unavailable`.

## Integração com Client Service

O client Feign usa a URL configurada em `CLIENT_SERVICE_URL`.

| Ambiente | Valor esperado |
| --- | --- |
| Local | `http://localhost:8081` |
| Docker | `http://client-api:8081` |

Contrato usado pelo `order-service`:

```http
GET /clients/{id}
X-User-Id: order-service
```

O header `X-User-Id` identifica a origem da chamada nos logs do `client-service`. Para chamadas feitas pelo `order-service`, o valor é fixo: `order-service`.

## Regras de negócio

- `clientId` é obrigatório.
- `description` é obrigatória.
- `amount` é obrigatório e deve ser maior que zero.
- Todo pedido nasce com status `CREATED`.
- Pedido só pode ser criado para cliente existente.
- Pedido só pode ser criado para cliente ativo.
- Pedido inexistente retorna `404 Not Found`.
- Falha de comunicação com o `client-service` retorna `503 Service Unavailable`.

## Regras de status

Transições permitidas:

| Status atual | Próximo status permitido |
| --- | --- |
| `CREATED` | `PROCESSING`, `CANCELLED` |
| `PROCESSING` | `COMPLETED`, `CANCELLED` |
| `COMPLETED` | Nenhuma |
| `CANCELLED` | Nenhuma |

Transições bloqueadas:

- `CREATED` -> `COMPLETED`
- `COMPLETED` -> qualquer outro status
- `CANCELLED` -> qualquer outro status
- aplicar o mesmo status novamente

Erros de transição retornam `409 Conflict`.

## Exemplos de requisição

### Health check público

```bash
curl http://localhost:8083/orders/public/test
```

Resposta:

```text
Order API public endpoint OK
```

### Criar pedido

```bash
curl -X POST http://localhost:8083/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: demo-order-001" \
  -d '{
    "clientId": 10,
    "description": "Compra de notebook",
    "amount": 3500.00
  }'
```

Resposta esperada:

```json
{
  "id": 1,
  "clientId": 10,
  "description": "Compra de notebook",
  "amount": 3500.00,
  "status": "CREATED",
  "createdAt": "2026-07-09T16:30:00",
  "updatedAt": "2026-07-09T16:30:00"
}
```

### Buscar pedido

```bash
curl http://localhost:8083/orders/1
```

### Listar pedidos

```bash
curl "http://localhost:8083/orders?page=0&size=10&sort=createdAt,desc"
```

### Atualizar status

```bash
curl -X PATCH "http://localhost:8083/orders/1/status?status=PROCESSING" \
  -H "X-Correlation-Id: demo-order-status-001"
```

## Exemplos de erro

### Payload inválido

```json
{
  "timestamp": "2026-07-09T16:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação",
  "path": "/orders",
  "correlationId": "demo-order-001",
  "errors": [
    {
      "field": "amount",
      "message": "O valor deve ser maior que zero"
    }
  ]
}
```

### Cliente inativo

```json
{
  "timestamp": "2026-07-09T16:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Cliente está inativo",
  "path": "/orders",
  "correlationId": "demo-order-002"
}
```

### Cliente inexistente

```json
{
  "timestamp": "2026-07-09T16:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Cliente não encontrado",
  "path": "/orders",
  "correlationId": "demo-order-003"
}
```

### Transição de status inválida

```json
{
  "timestamp": "2026-07-09T16:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Transição inválida de CREATED para COMPLETED",
  "path": "/orders/1/status",
  "correlationId": "demo-order-004"
}
```

### Client Service indisponível

```json
{
  "timestamp": "2026-07-09T16:30:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Client-service indisponível. Não foi possível validar o cliente no momento.",
  "path": "/orders",
  "correlationId": "demo-order-005"
}
```

## Execução local

Para executar o `order-service` localmente, suba primeiro o `client-service` na porta `8081`. Em seguida, execute:

```bash
cd order-service
mvn spring-boot:run
```

Configuração local padrão:

```text
ORDER SERVICE: http://localhost:8083
CLIENT SERVICE: http://localhost:8081
DATABASE: H2 em memória
EUREKA: desabilitado no profile default
```

Se quiser alterar a URL do `client-service`:

```bash
CLIENT_SERVICE_URL=http://localhost:8081 mvn spring-boot:run
```

No PowerShell:

```powershell
$env:CLIENT_SERVICE_URL="http://localhost:8081"
mvn spring-boot:run
```

## Execução com Docker

Na raiz do projeto, configure o `.env` com base no `.env.example` e execute:

```bash
docker compose up --build
```

Serviços usados pelo fluxo completo:

- `postgres-client`
- `postgres-order`
- `service-registry`
- `client-api`
- `order-service`
- `api-gateway`

No Docker, o `order-service` usa:

```text
CLIENT_SERVICE_URL=http://client-api:8081
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-registry:8761/eureka
```

## Swagger

Com o serviço em execução, acesse:

```text
http://localhost:8083/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8083/v3/api-docs
```

## Testes

Execute na pasta do serviço:

```bash
mvn clean test
```

Os testes cobrem:

- contrato do Feign Client;
- fallback do `client-service`;
- criação de pedido com cliente ativo;
- bloqueio de cliente inexistente;
- bloqueio de cliente inativo;
- validação de valor menor ou igual a zero;
- transições válidas e inválidas de status;
- respostas de erro padronizadas.

## Variáveis de ambiente

| Variável | Descrição | Exemplo |
| --- | --- | --- |
| `CLIENT_SERVICE_URL` | URL base do `client-service` usada pelo Feign | `http://localhost:8081` |
| `SPRING_DATASOURCE_URL` | URL JDBC do banco em ambiente Docker/prod | `jdbc:postgresql://postgres-order:5432/orderdb` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco de pedidos | `order` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco de pedidos | `order` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | URL do Eureka Server | `http://service-registry:8761/eureka` |

## Observabilidade da requisição

As respostas de erro retornam e propagam o header `X-Correlation-Id`.

Exemplo:

```bash
curl -X POST http://localhost:8083/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: demo-correlation-001" \
  -d '{
    "clientId": 999999,
    "description": "Pedido de teste",
    "amount": 100.00
  }'
```

O mesmo valor enviado em `X-Correlation-Id` volta no header e no corpo da resposta de erro.
