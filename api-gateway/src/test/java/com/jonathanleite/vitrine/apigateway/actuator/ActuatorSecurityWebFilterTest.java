package com.jonathanleite.vitrine.apigateway.actuator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonathanleite.vitrine.apigateway.security.AuthenticationErrorWriter;
import com.jonathanleite.vitrine.apigateway.security.BearerTokenExtractor;
import com.jonathanleite.vitrine.apigateway.security.JwtAuthenticationService;
import com.jonathanleite.vitrine.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActuatorSecurityWebFilterTest {

    private JwtUtil jwtUtil;
    private ActuatorSecurityWebFilter filter;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        filter = new ActuatorSecurityWebFilter(
                new BearerTokenExtractor(),
                new JwtAuthenticationService(jwtUtil),
                new AuthenticationErrorWriter(new ObjectMapper())
        );
    }

    @Test
    void shouldAllowHealthWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health")
        );
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, successfulChain(chainCalled)).block();

        assertThat(chainCalled).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void shouldRejectInfoWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/info")
        );
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, successfulChain(chainCalled)).block();

        assertThat(chainCalled).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE))
                .startsWith("application/json");
        assertThat(exchange.getResponse().getBodyAsString().block())
                .contains("\"message\":\"Token ausente\"")
                .contains("\"path\":\"/actuator/info\"")
                .contains("\"correlationId\":");
    }

    @Test
    void shouldRejectNestedMetricsWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/metrics/jvm.memory.used")
        );
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, successfulChain(chainCalled)).block();

        assertThat(chainCalled).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectActuatorIndexWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator")
        );
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, successfulChain(chainCalled)).block();

        assertThat(chainCalled).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldAllowProtectedActuatorEndpointWithValidToken() {
        Claims claims = mock(Claims.class);
        when(jwtUtil.validateToken("valid-token")).thenReturn(claims);
        when(jwtUtil.extractUsername(claims)).thenReturn("actuator-admin");

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/metrics")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
        );
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.filter(exchange, successfulChain(chainCalled)).block();

        assertThat(chainCalled).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    private WebFilterChain successfulChain(AtomicBoolean chainCalled) {
        return exchange -> {
            chainCalled.set(true);
            return Mono.empty();
        };
    }
}
