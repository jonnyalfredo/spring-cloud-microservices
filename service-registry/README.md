# Service Registry

O `service-registry` e o servidor Eureka da arquitetura Vitrine API. Ele permite que os microsservicos encontrem uns aos outros pelo nome logico, sem depender de URLs fixas espalhadas pelo codigo.

Este modulo nao implementa regra de negocio. Sua responsabilidade e operacional: manter o catalogo de instancias disponiveis para que o gateway e os servicos internos consigam se comunicar de forma dinamica.

## Objetivo

Em uma arquitetura de microsservicos, cada servico pode subir em um host, porta ou container diferente. O Service Registry centraliza essa descoberta.

No projeto, os servicos registram sua localizacao no Eureka e passam a ser encontrados por nomes como:

- `client-api`;
- `order-service`;
- `api-gateway`.

Com isso, o API Gateway pode rotear para `lb://client-api` e `lb://order-service`, enquanto o Spring Cloud LoadBalancer resolve a instancia disponivel.

## Service discovery

Service discovery e o mecanismo que permite encontrar servicos em tempo de execucao.

Fluxo resumido:

```text
client-api / order-service / api-gateway
  -> registram-se no Eureka
  -> enviam heartbeats periodicos
  -> Eureka mantem o catalogo de instancias
  -> consumidores usam o nome do servico para localizar uma instancia ativa
```

Esse padrao evita acoplamento forte com endereco IP e facilita execucao local, Docker e ambientes distribuidos.

## Porta e dashboard

O Service Registry roda na porta:

```text
8761
```

Dashboard do Eureka:

```text
http://localhost:8761
```

No dashboard e possivel visualizar as aplicacoes registradas e o status de cada instancia.

## Servicos esperados no Eureka

No ambiente completo, os seguintes servicos devem aparecer registrados:

| Servico | Papel |
| --- | --- |
| `client-api` | Cadastro e consulta de clientes |
| `order-service` | Criacao e controle de pedidos |
| `api-gateway` | Entrada HTTP externa da arquitetura |

O proprio `service-registry` nao precisa se registrar nele mesmo.

## Configuracoes principais

### `register-with-eureka=false`

Indica que o proprio Eureka Server nao deve se registrar como cliente Eureka.

Neste projeto o `service-registry` e o servidor central, entao ele apenas recebe registros dos demais servicos.

### `fetch-registry=false`

Indica que o Eureka Server nao precisa buscar o catalogo de outro registry.

Como este projeto usa um unico Eureka Server, nao existe outro registry para sincronizar.

### `enable-self-preservation`

Self-preservation e um mecanismo de protecao do Eureka. Quando ativo, o servidor evita remover instancias do catalogo de forma agressiva em cenarios de instabilidade de rede.

No projeto:

| Profile | Valor | Motivo |
| --- | --- | --- |
| `default` | `false` | Desenvolvimento local remove instancias paradas mais rapido |
| `docker` | `false` | Facilita testes com containers reiniciando |
| `prod` | `true` | Evita remocao indevida de instancias saudaveis em instabilidade |

## Profiles

### Local/default

Configuracao pensada para desenvolvimento local:

```yaml
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 10000
  instance:
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30
```

### Docker

Mantem comportamento equivalente ao local para facilitar validacao durante `docker compose up`.

```yaml
spring:
  profiles:
    active: docker
```

O compose sobe o registry antes dos servicos dependentes e usa health check em `/actuator/health`.

### Producao

Profile mais conservador:

```yaml
eureka:
  server:
    enable-self-preservation: true
    eviction-interval-timer-in-ms: 60000
  instance:
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
```

## Actuator

Somente os endpoints operacionais necessarios ficam expostos:

| Endpoint | Uso |
| --- | --- |
| `/actuator/health` | Verificar se o registry esta UP |
| `/actuator/info` | Consultar nome, descricao e versao do modulo |

Health check:

```bash
curl http://localhost:8761/actuator/health
```

Resposta esperada:

```json
{
  "status": "UP"
}
```

Info:

```bash
curl http://localhost:8761/actuator/info
```

Resposta esperada:

```json
{
  "app": {
    "name": "Service Registry",
    "description": "Eureka Server responsavel pelo service discovery",
    "version": "1.0.0"
  }
}
```

Endpoints sensiveis, como `/actuator/env`, nao ficam expostos.

## Execucao local

Entre na pasta do modulo:

```bash
cd service-registry
```

Execute os testes:

```bash
mvn clean test
```

Suba a aplicacao:

```bash
mvn spring-boot:run
```

Acesse:

```text
http://localhost:8761
```

## Execucao com Docker

Na raiz do projeto:

```bash
docker compose up --build service-registry
```

Para subir todo o ambiente:

```bash
docker compose up --build
```

O `docker-compose.yml` valida a saude do registry com:

```bash
wget -q -O - http://localhost:8761/actuator/health
```

## Configuracao dos clientes Eureka

Servicos que precisam se registrar no Eureka usam uma configuracao como:

```yaml
eureka:
  client:
    enabled: true
    service-url:
      defaultZone: http://service-registry:8761/eureka
  instance:
    prefer-ip-address: true
```

No Docker Compose, a URL e enviada por variavel de ambiente:

```text
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-registry:8761/eureka
```

No ambiente local, normalmente a URL aponta para:

```text
http://localhost:8761/eureka
```

## Checklist de validacao

- `mvn clean test` passa no modulo.
- `mvn spring-boot:run` sobe o Eureka na porta `8761`.
- `http://localhost:8761` abre o dashboard.
- `/actuator/health` retorna `UP`.
- `/actuator/info` retorna nome, descricao e versao.
- `client-api`, `order-service` e `api-gateway` aparecem no dashboard quando o ambiente completo esta em execucao.
