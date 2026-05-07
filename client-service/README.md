# Client API – Projeto Vitrine Backend Java

Este projeto foi desenvolvido com o objetivo de **servir como vitrine profissional** para entrevistas de **Desenvolvedor Backend Java**, demonstrando boas práticas, arquitetura limpa e preocupações reais de ambiente corporativo.

---

## 🎯 Objetivo do Projeto

* Demonstrar domínio de **Spring Boot** e ecossistema Java
* Aplicar **boas práticas de arquitetura e design**
* Construir uma API REST **realista**, próxima do dia a dia em empresas
* Mostrar preocupação com **testes, versionamento, Docker e organização**

---

## 🧱 Stack Utilizada

* **Java 17**
* **Spring Boot**
* Spring Web
* Spring Data JPA
* Bean Validation
* **PostgreSQL**
* **Docker & Docker Compose**
* JUnit 5
* Mockito
* Maven

---

## 🏗️ Arquitetura

O projeto segue uma separação clara de responsabilidades:

* **Controller** → Camada de entrada (REST)
* **Service** → Regras de negócio
* **Repository** → Acesso a dados
* **DTOs** → Contrato da API
* **Exceptions customizadas** → Tratamento de erros claro e consistente
* **ControllerAdvice** → Padronização de respostas de erro

Essa estrutura facilita manutenção, testes e evolução da aplicação.

---

## 🚀 Funcionalidades Implementadas

### ✅ CRUD completo de Clientes

* Criar cliente
* Atualizar cliente (PUT)
* Atualização parcial (PATCH)
* Buscar cliente por ID
* Listar clientes
* Remover cliente

### ✅ Paginação e Ordenação

Endpoint:

```
GET /clients?page=0&size=10&sort=email,asc
```

* Paginação utilizando `Pageable`
* Ordenação dinâmica por qualquer campo

### ✅ Filtros Dinâmicos

```
GET /clients?email=teste@email.com
GET /clients?document=123456789
```

* Filtros opcionais via query params
* Implementação preparada para crescimento

### ✅ Validações de Negócio

* Email único
* Documento único
* Validações com Bean Validation (`@Valid`)

### ✅ Tratamento Global de Erros

* `ResourceNotFoundException`
* `ConflictException`
* `BusinessException`

Respostas padronizadas e claras para o consumidor da API.

---

## 🧪 Testes

* Testes unitários completos de **Service**
* Testes de **Controller** com MockMvc
* Uso de Mockito para isolamento de dependências
* Profile de teste separado (`application-test.yml`)

O objetivo dos testes é garantir:

* Regras de negócio
* Contrato da API
* Evolução segura do código

---

## 🐳 Docker

O projeto está preparado para rodar em ambiente containerizado.

### Subir a aplicação com Docker

```bash
docker-compose up --build
```

Containers:

* **API Spring Boot**
* **PostgreSQL**

Profile utilizado no Docker:

```
SPRING_PROFILES_ACTIVE=prod
```

---

## ⚙️ Configurações por Ambiente

* `application.yml` → ambiente local/dev
* `application-test.yml` → testes
* `application-prod.yml` → produção (Docker)

Separação pensada para facilitar deploy e segurança de configurações.

---

## 📌 Possíveis Evoluções

Este projeto foi pensado para ser simples, mas com espaço para crescimento:

* Autenticação e autorização (Spring Security / JWT)
* Versionamento de API
* Cache com Redis
* Observabilidade (Actuator + Prometheus)
* CI/CD
* OpenAPI / Swagger

---

## 🧑‍💻 Considerações Finais

Este projeto não foi feito apenas para "funcionar", mas para **demonstrar maturidade técnica**, preocupação com qualidade e práticas reais de mercado.

Ele reflete decisões que eu tomaria em um ambiente profissional.

---

📎 **Autor**: Jonathan Leite

📌 Projeto desenvolvido para fins de estudo e portfólio profissional.
