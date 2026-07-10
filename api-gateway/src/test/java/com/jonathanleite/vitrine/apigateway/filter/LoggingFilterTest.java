package com.jonathanleite.vitrine.apigateway.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
class LoggingFilterTest {

    private final LoggingFilter filter = new LoggingFilter();

    @Test
    void shouldLogCorrelationIdWithoutLoggingJwt(CapturedOutput output) {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
                        .header("X-Correlation-Id", "corr-log-success")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer header.payload.signature")
        );

        GatewayFilterChain chain = nextExchange -> {
            nextExchange.getResponse().setStatusCode(HttpStatus.OK);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(output).contains("CorrelationId: corr-log-success");
        assertThat(output).doesNotContain("header.payload.signature");
        assertThat(output).doesNotContain("Bearer");
    }

    @Test
    void shouldLogCorrelationIdWhenRequestFails(CapturedOutput output) {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
                        .header("X-Correlation-Id", "corr-log-error")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer secret.jwt.value")
        );

        GatewayFilterChain chain = nextExchange -> Mono.error(new IllegalStateException("boom"));

        assertThatThrownBy(() -> filter.filter(exchange, chain).block())
                .isInstanceOf(IllegalStateException.class);

        assertThat(output).contains("CorrelationId: corr-log-error");
        assertThat(output).contains("Request failed");
        assertThat(output).doesNotContain("secret.jwt.value");
        assertThat(output).doesNotContain("Bearer");
    }
}
