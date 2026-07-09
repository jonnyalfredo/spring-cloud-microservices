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

## Regras de status

- Status inicial: `CREATED`.
- `CREATED` não pode ir direto para `COMPLETED`.
- Pedidos `COMPLETED` ou `CANCELLED` não podem ser alterados.

## Rodar testes

```bash
mvn clean test
```
