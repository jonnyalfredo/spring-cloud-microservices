# API Gateway

O `api-gateway` centraliza a entrada HTTP dos microsservicos do projeto e aplica autenticacao JWT, correlation ID, logging, roteamento, retry, circuit breaker e fallback.

## Resiliencia

As politicas foram definidas para evitar duplicidade em operacoes de escrita:

- Retry automatico fica limitado a `GET`.
- `POST`, `PATCH`, `PUT` e `DELETE` nao possuem retry automatico no gateway.
- Cada retry usa no maximo `2` novas tentativas para falhas transientes de infraestrutura.
- Timeouts do gateway:
  - `connect-timeout`: `2000ms`
  - `response-timeout`: `5s`
- Cada servico possui circuit breaker e fallback proprio:
  - `client-api` -> `clientServiceCircuitBreaker` -> `/fallback/client`
  - `order-service` -> `orderServiceCircuitBreaker` -> `/fallback/order`

Com isso, uma criacao ou atualizacao que falhe nao e reenviada automaticamente pelo gateway, reduzindo risco de duplicidade. Leituras podem ser repetidas porque sao idempotentes.

## Circuit Breaker

Configuracao aplicada por servico:

- janela baseada em quantidade de chamadas;
- `slidingWindowSize`: `10`;
- `minimumNumberOfCalls`: `5`;
- `failureRateThreshold`: `50`;
- `waitDurationInOpenState`: `10s`;
- `permittedNumberOfCallsInHalfOpenState`: `3`.

Quando o circuito abre, o gateway retorna fallback em JSON padronizado com status `503 Service Unavailable` e `X-Correlation-Id`.
