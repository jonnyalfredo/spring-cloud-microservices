package com.jonathanleite.vitrine.apigateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

class FallbackControllerTest {

    private final WebTestClient webTestClient = WebTestClient
            .bindToController(new FallbackController())
            .build();

    @Test
    void shouldReturnStandardJsonWhenClientServiceIsUnavailable() {
        webTestClient.get()
                .uri("/fallback/client")
                .header("X-Correlation-Id", "corr-client-fallback")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().valueEquals("X-Correlation-Id", "corr-client-fallback")
                .expectBody()
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.error").isEqualTo("Service Unavailable")
                .jsonPath("$.message").isEqualTo(
                        "Client-service indisponivel. Nao foi possivel encaminhar a requisicao no momento."
                )
                .jsonPath("$.path").isEqualTo("/fallback/client")
                .jsonPath("$.correlationId").isEqualTo("corr-client-fallback")
                .jsonPath("$.timestamp").isNotEmpty();
    }

    @Test
    void shouldReturnStandardJsonWhenOrderServiceIsUnavailable() {
        webTestClient.get()
                .uri("/fallback/order")
                .header("X-Correlation-Id", "corr-order-fallback")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().valueEquals("X-Correlation-Id", "corr-order-fallback")
                .expectBody()
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.error").isEqualTo("Service Unavailable")
                .jsonPath("$.message").isEqualTo(
                        "Order-service indisponivel. Nao foi possivel encaminhar a requisicao no momento."
                )
                .jsonPath("$.path").isEqualTo("/fallback/order")
                .jsonPath("$.correlationId").isEqualTo("corr-order-fallback")
                .jsonPath("$.timestamp").isNotEmpty();
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsMissing() {
        webTestClient.get()
                .uri("/fallback/client")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().exists("X-Correlation-Id")
                .expectBody()
                .jsonPath("$.correlationId").isNotEmpty();
    }
}
