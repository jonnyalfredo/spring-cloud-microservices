# Order Service

Microservico responsavel pela criacao e gerenciamento de pedidos.

Este servico faz parte do projeto principal `spring-cloud-microservices`. A documentacao completa de arquitetura, Docker, variaveis de ambiente e exemplos de requisicoes esta no [README da raiz](../README.md).

## Responsabilidades

- Criar pedidos.
- Consultar clientes no `client-service` via OpenFeign.
- Bloquear pedido para cliente inexistente.
- Bloquear pedido para cliente inativo.
- Bloquear valor menor ou igual a zero.
- Controlar transicoes de status.
- Bloquear alteracao de pedido finalizado.
- Retornar erros no formato padronizado do projeto.

## Porta

```text
8083
```

## Endpoints principais

| Metodo | Endpoint | Descricao |
| --- | --- | --- |
| `GET` | `/orders/public/test` | Rota publica de teste |
| `POST` | `/orders` | Criar pedido |
| `GET` | `/orders/{id}` | Buscar pedido |
| `GET` | `/orders?page=0&size=10` | Listar pedidos |
| `PATCH` | `/orders/{id}/status?status=PROCESSING` | Atualizar status |

## Regras de status

- Status inicial: `CREATED`.
- `CREATED` nao pode ir direto para `COMPLETED`.
- Pedidos `COMPLETED` ou `CANCELLED` nao podem ser alterados.

## Rodar testes

```bash
mvn clean test
```
