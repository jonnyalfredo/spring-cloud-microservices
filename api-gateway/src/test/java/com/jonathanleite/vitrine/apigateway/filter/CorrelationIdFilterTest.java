package com.jonathanleite.vitrine.apigateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void shouldPropagateExistingCorrelationId() {
        AtomicReference<String> propagatedCorrelationId = new AtomicReference<>();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
                        .header("X-Correlation-Id", "corr-123")
        );

        GatewayFilterChain chain = capturedExchange -> {
            propagatedCorrelationId.set(
                    capturedExchange.getRequest().getHeaders().getFirst("X-Correlation-Id")
            );
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(propagatedCorrelationId.get()).isEqualTo("corr-123");
        assertThat(exchange.getResponse().getHeaders().getFirst("X-Correlation-Id"))
                .isEqualTo("corr-123");
    }

    @Test
    void shouldGenerateCorrelationIdWhenMissing() {
        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
        );

        GatewayFilterChain chain = nextExchange -> {
            capturedExchange.set(nextExchange);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst("X-Correlation-Id"))
                .isNotBlank();
        assertThat(exchange.getResponse().getHeaders().getFirst("X-Correlation-Id"))
                .isEqualTo(capturedExchange.get().getRequest().getHeaders().getFirst("X-Correlation-Id"));
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsBlank() {
        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
                        .header("X-Correlation-Id", " ")
        );

        GatewayFilterChain chain = nextExchange -> {
            capturedExchange.set(nextExchange);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        String correlationId = capturedExchange.get().getRequest().getHeaders().getFirst("X-Correlation-Id");

        assertThat(correlationId).isNotBlank();
        assertThat(correlationId).isNotEqualTo(" ");
        assertThat(exchange.getResponse().getHeaders().getFirst("X-Correlation-Id"))
                .isEqualTo(correlationId);
    }

    @Test
    void shouldKeepCorrelationIdHeaderWhenChainFails() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
                        .header("X-Correlation-Id", "corr-internal-error")
        );

        GatewayFilterChain chain = nextExchange -> {
            nextExchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return Mono.error(new IllegalStateException("unexpected failure"));
        };

        assertThatThrownBy(() -> filter.filter(exchange, chain).block())
                .isInstanceOf(IllegalStateException.class);

        assertThat(exchange.getResponse().getHeaders().getFirst("X-Correlation-Id"))
                .isEqualTo("corr-internal-error");
    }
}
