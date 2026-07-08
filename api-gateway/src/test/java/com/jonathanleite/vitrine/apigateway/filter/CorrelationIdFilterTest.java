package com.jonathanleite.vitrine.apigateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

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
    }
}
