# API Gateway

O `api-gateway` e a porta de entrada HTTP da arquitetura Vitrine API. Ele concentra roteamento, autenticacao, rastreabilidade, resiliencia e fallbacks antes de encaminhar as requisicoes para os microsservicos internos.

Em um ambiente real, esse servico evita que clientes externos chamem diretamente `client-service` e `order-service`, padroniza headers importantes e reduz duplicidade de regras transversais espalhadas pelos servicos de negocio.

## Papel na arquitetura

Fluxo principal:

```text
Cliente externo
  -> API Gateway :8080
  -> Eureka Service Discovery
  -> client-service / order-service
  -> PostgreSQL de cada dominio
```

Responsabilidades do gateway:

- expor uma entrada unica em `http://localhost:8080`;
- resolver servicos pelo Eureka usando `lb://client-api` e `lb://order-service`;
- validar JWT nas rotas protegidas;
- propagar a identidade interna pelo header `X-User-Id`;
- gerar ou preservar `X-Correlation-Id`;
- registrar logs de entrada, saida, status e tempo de resposta;
- aplicar retry seguro apenas para `GET`;
- aplicar circuit breaker por servico;
- retornar fallbacks padronizados quando um servico estiver indisponivel;
- expor health checks operacionais com Actuator.

## Rotas disponiveis

| Entrada no gateway | Destino interno | Regra aplicada |
| --- | --- | --- |
| `/api/v1/clients` | `lb://client-api/clients` | `StripPrefix=2` |
| `/api/v1/clients/**` | `lb://client-api/clients/**` | `StripPrefix=2` |
| `/api/v1/orders` | `lb://order-service/orders` | `StripPrefix=2` |
| `/api/v1/orders/**` | `lb://order-service/orders/**` | `StripPrefix=2` |

O gateway usa `StripPrefix=2`, entao `/api/v1/clients/123` chega no `client-service` como `/clients/123`.

## Eureka

No profile `docker`, o gateway habilita o Eureka Client e consulta os servicos registrados pelo nome:

- `client-api`;
- `order-service`.

Configuracao principal:

```yaml
eureka:
  client:
    enabled: true
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}
```

Em execucao local sem Docker, o Eureka fica desabilitado por padrao no `application.yml`. Para rodar com service discovery localmente, informe as variaveis adequadas e habilite o client conforme o profile usado.

## Autenticacao JWT

Rotas protegidas exigem:

```http
Authorization: Bearer <jwt>
```

O gateway valida o token com `JWT_SECRET` e usa o `sub` do JWT como identificador do usuario.

Comportamento importante:

- requisicoes sem token retornam `401 Unauthorized`;
- token malformado, invalido ou expirado retorna `401 Unauthorized`;
- token sem identificacao do usuario retorna `401 Unauthorized`;
- o header `Authorization` e removido antes de encaminhar para os servicos internos;
- qualquer `X-User-Id` recebido do cliente externo e descartado;
- o `X-User-Id` encaminhado internamente sempre vem do JWT.

## Rotas publicas e protegidas

Rotas publicas configuradas:

- `/api/v1/auth/login`;
- `/api/v1/clients/public/test`;
- `/api/v1/orders/public/test`;
- `/actuator/health`;
- `/actuator/health/**`.

Todas as demais rotas sao protegidas por padrao. O gateway nao libera uma rota apenas porque o caminho contem palavras como `auth` ou `public`.

Endpoints administrativos protegidos:

- `/actuator`;
- `/actuator/info`;
- `/actuator/metrics`;
- `/actuator/gateway`;
- `/actuator/gateway/**`.

## Correlation ID

O gateway usa o header:

```http
X-Correlation-Id: <uuid-ou-id-informado>
```

Regras:

- se o cliente enviar `X-Correlation-Id`, o valor e preservado;
- se o cliente nao enviar, o gateway gera um UUID;
- o mesmo valor e enviado aos microsservicos internos;
- o mesmo valor volta na resposta;
- erros de autenticacao e fallbacks tambem retornam `correlationId` no corpo JSON.

## Logging

O gateway registra:

- metodo HTTP;
- path;
- IP de origem quando disponivel;
- correlation ID;
- status HTTP da resposta;
- tempo total da requisicao;
- classe do erro em falhas.

O log nao deve expor JWT completo, segredos ou dados sensiveis.

## Retry

Retry automatico foi limitado a operacoes idempotentes:

- aplicado somente a `GET`;
- nao aplicado a `POST`, `PATCH`, `PUT` ou `DELETE`;
- evita duplicidade em operacoes de escrita;
- usa no maximo `2` novas tentativas;
- considera falhas transientes como `BAD_GATEWAY`, `GATEWAY_TIMEOUT` e `INTERNAL_SERVER_ERROR`.

Timeouts configurados:

- `connect-timeout`: `2000ms`;
- `response-timeout`: `5s`.

## Circuit breaker

Cada servico possui circuit breaker proprio:

| Servico | Circuit breaker | Fallback |
| --- | --- | --- |
| `client-api` | `clientServiceCircuitBreaker` | `/fallback/client` |
| `order-service` | `orderServiceCircuitBreaker` | `/fallback/order` |

Configuracao aplicada:

- janela por quantidade de chamadas;
- `slidingWindowSize`: `10`;
- `minimumNumberOfCalls`: `5`;
- `failureRateThreshold`: `50`;
- `waitDurationInOpenState`: `10s`;
- `permittedNumberOfCallsInHalfOpenState`: `3`;
- transicao automatica para half-open habilitada.

## Fallbacks

Quando um servico interno esta indisponivel, o gateway retorna `503 Service Unavailable` em JSON padronizado.

Fallback do client-service:

```json
{
  "timestamp": "2026-07-10T16:00:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Client-service indisponivel. Nao foi possivel encaminhar a requisicao no momento.",
  "path": "/fallback/client",
  "correlationId": "corr-demo-001"
}
```

Fallback do order-service:

```json
{
  "timestamp": "2026-07-10T16:00:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Order-service indisponivel. Nao foi possivel encaminhar a requisicao no momento.",
  "path": "/fallback/order",
  "correlationId": "corr-demo-001"
}
```

## Actuator

O gateway expoe somente os endpoints operacionais necessarios:

| Endpoint | Acesso | Uso |
| --- | --- | --- |
| `/actuator/health` | Publico | Health check local e Docker |
| `/actuator/info` | Protegido por JWT | Informacoes do servico |
| `/actuator/metrics` | Protegido por JWT | Metricas operacionais |
| `/actuator/gateway` | Protegido por JWT | Dados administrativos do Spring Cloud Gateway |

O health check nao exibe detalhes internos sensiveis. O endpoint `/actuator/info` informa nome, descricao e versao do servico.

No Docker Compose, a saude do gateway e verificada com:

```bash
wget -q -O - http://localhost:8080/actuator/health
```

## Variaveis de ambiente

| Variavel | Obrigatoria | Exemplo | Descricao |
| --- | --- | --- | --- |
| `JWT_SECRET` | Sim | `local-validation-secret-with-at-least-32-characters` | Segredo usado para validar JWT |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Docker | `http://service-registry:8761/eureka` | URL do Eureka usada pelo gateway |
| `SPRING_PROFILES_ACTIVE` | Docker | `docker` | Ativa configuracoes de container |

No `docker-compose.yml`, `JWT_SECRET` e `EUREKA_DEFAULT_ZONE` devem estar definidos no ambiente ou em um arquivo `.env`.

## Execucao local

Entre na pasta do gateway:

```bash
cd api-gateway
```

Execute os testes:

```bash
mvn clean test
```

Execute a aplicacao:

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djwt.secret=local-validation-secret-with-at-least-32-characters"
```

Por padrao, o gateway sobe em:

```text
http://localhost:8080
```

## Execucao via Docker

Na raiz do projeto, defina as variaveis de ambiente esperadas pelo compose ou use o `.env` local:

```bash
JWT_SECRET=local-validation-secret-with-at-least-32-characters
EUREKA_DEFAULT_ZONE=http://service-registry:8761/eureka
```

Suba o ambiente:

```bash
docker compose up --build
```

Verifique o health do gateway:

```bash
curl http://localhost:8080/actuator/health
```

Resposta esperada:

```json
{
  "status": "UP"
}
```

## Exemplos

### Requisicao autenticada

```bash
curl -X GET http://localhost:8080/api/v1/clients \
  -H "Authorization: Bearer <jwt-valido>" \
  -H "X-Correlation-Id: corr-demo-001"
```

O gateway encaminha internamente:

```http
X-User-Id: <sub-do-jwt>
X-Correlation-Id: corr-demo-001
```

### Erro 401

Requisicao sem token:

```bash
curl -i http://localhost:8080/api/v1/clients
```

Resposta esperada:

```json
{
  "timestamp": "2026-07-10T16:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token ausente",
  "path": "/api/v1/clients",
  "correlationId": "b5f3f4d1-66d9-40e5-9270-5d4f7a7f4c4e"
}
```

### Fallback 503

Quando o `client-service` esta indisponivel:

```bash
curl -i http://localhost:8080/api/v1/clients \
  -H "Authorization: Bearer <jwt-valido>" \
  -H "X-Correlation-Id: corr-demo-001"
```

Resposta esperada:

```json
{
  "timestamp": "2026-07-10T16:00:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Client-service indisponivel. Nao foi possivel encaminhar a requisicao no momento.",
  "path": "/fallback/client",
  "correlationId": "corr-demo-001"
}
```

## Checklist de validacao

- `mvn clean test` deve passar no modulo `api-gateway`;
- `docker compose up --build` deve subir o gateway na porta `8080`;
- `/actuator/health` deve retornar `UP`;
- `/actuator/info`, `/actuator/metrics` e `/actuator/gateway/routes` devem exigir JWT;
- chamadas externas devem passar pelo gateway, sem necessidade de acessar diretamente os servicos internos.
