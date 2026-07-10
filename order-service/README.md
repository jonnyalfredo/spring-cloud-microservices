# Order Service

Microsserviço responsável pela criação e gerenciamento de pedidos.

Este serviço faz parte do projeto principal `spring-cloud-microservices`. A documentação completa de arquitetura, Docker, variáveis de ambiente e exemplos de requisições está no [README da raiz](../README.md).

## Responsabilidades

- Criar pedidos.
- Consultar clientes no `client-service` via OpenFeign.
- Bloquear pedido para cliente inexistente.
- Bloquear pedido para cliente inativo.
- Bloquear valor menor ou igual a zero.
- Controlar transições de status.
- Bloquear alteração de pedido finalizado.
- Retornar erros no formato padronizado do projeto.
- Retornar datas de criação e atualização para rastreabilidade.

## Porta

```text
8083
```

## Endpoints principais

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `GET` | `/orders/public/test` | Rota pública de teste |
| `POST` | `/orders` | Criar pedido |
| `GET` | `/orders/{id}` | Buscar pedido |
| `GET` | `/orders?page=0&size=10` | Listar pedidos |
| `PATCH` | `/orders/{id}/status?status=PROCESSING` | Atualizar status |

## Exemplo de resposta

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

## Integração com Client Service

Ao criar um pedido, o `order-service` consulta o `client-service` via OpenFeign antes de salvar o registro.

Contrato da integração:

- Endpoint consultado: `GET /clients/{id}`.
- Header enviado: `X-User-Id: order-service`.
- Cliente existente e ativo: pedido é criado.
- Cliente inexistente: retorna `404 Not Found`.
- Cliente inativo: retorna `400 Bad Request`.
- `client-service` indisponível: retorna `503 Service Unavailable` com mensagem clara.

## Regras de status

- Status inicial: `CREATED`.
- Transições permitidas:
  - `CREATED` -> `PROCESSING`
  - `PROCESSING` -> `COMPLETED`
  - `CREATED` -> `CANCELLED`
  - `PROCESSING` -> `CANCELLED`
- Transições bloqueadas:
  - `CREATED` -> `COMPLETED`
  - alterar pedido `COMPLETED`
  - alterar pedido `CANCELLED`
  - aplicar o mesmo status novamente

## Rodar testes

```bash
mvn clean test
```
