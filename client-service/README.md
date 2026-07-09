# Client Service

Microsserviço responsável pelo cadastro e gerenciamento de clientes.

Este serviço faz parte do projeto principal `spring-cloud-microservices`. A documentação completa de arquitetura, Docker, variáveis de ambiente e exemplos de requisições está no [README da raiz](../README.md).

## Responsabilidades

- Criar clientes.
- Bloquear email duplicado.
- Bloquear documento duplicado.
- Buscar cliente por ID.
- Listar clientes com paginação e filtros.
- Atualizar cliente com `PUT`.
- Atualizar cliente parcialmente com `PATCH`.
- Inativar cliente por exclusão lógica.
- Retornar erros no formato padronizado do projeto.

## Porta

```text
8081
```

## Endpoints principais

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `GET` | `/clients/public/test` | Rota pública de teste |
| `POST` | `/clients` | Criar cliente |
| `GET` | `/clients/{id}` | Buscar cliente |
| `GET` | `/clients?page=0&size=10` | Listar clientes |
| `PUT` | `/clients/{id}` | Atualizar cliente |
| `PATCH` | `/clients/{id}` | Atualizar parcialmente |
| `PATCH` | `/clients/{id}/activate` | Ativar cliente |
| `PATCH` | `/clients/{id}/deactivate` | Inativar cliente |
| `DELETE` | `/clients/{id}` | Inativar cliente por exclusão lógica |

Rotas privadas acessadas diretamente exigem o header:

```text
X-User-Id: local-user
```

## Rodar testes

```bash
mvn clean test
```
