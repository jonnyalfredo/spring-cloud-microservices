package com.jonathanleite.vitrine.apigateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonathanleite.vitrine.apigateway.config.PublicRouteProperties;
import com.jonathanleite.vitrine.apigateway.exception.JwtAuthenticationException;
import com.jonathanleite.vitrine.apigateway.security.AuthenticationErrorWriter;
import com.jonathanleite.vitrine.apigateway.security.BearerTokenExtractor;
import com.jonathanleite.vitrine.apigateway.security.JwtAuthenticationService;
import com.jonathanleite.vitrine.apigateway.security.PublicRouteMatcher;
import com.jonathanleite.vitrine.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationFilterTest {

    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PublicRouteProperties publicRouteProperties = publicRouteProperties();
    private final AuthenticationFilter filter = new AuthenticationFilter(
            new PublicRouteMatcher(publicRouteProperties),
            new BearerTokenExtractor(),
            new JwtAuthenticationService(jwtUtil),
            new AuthenticationErrorWriter(objectMapper)
    );

    @Test
    void shouldAllowPublicRouteWithoutToken() {
        assertPublicRouteAllowed("/api/v1/auth/login");
        assertPublicRouteAllowed("/api/v1/clients/public/test");
        assertPublicRouteAllowed("/api/v1/orders/public/test");
        assertPublicRouteAllowed("/actuator/health");
        assertPublicRouteAllowed("/actuator/health/readiness");
    }

    @Test
    void shouldRejectAmbiguousRoutesContainingPublicOrAuth() throws Exception {
        List<String> ambiguousPaths = List.of(
                "/api/v1/orders/public-data",
                "/api/v1/clients/my-auth-history",
                "/api/v1/publications"
        );

        for (String path : ambiguousPaths) {
            GatewayFilterChain chain = mock(GatewayFilterChain.class);
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get(path)
                            .header("X-Correlation-Id", "corr-ambiguous-route")
            );

            filter.filter(exchange, chain).block();

            assertUnauthorizedError(exchange, "Token ausente", "corr-ambiguous-route", path);
            verify(chain, never()).filter(any());
        }
    }

    @Test
    void shouldRejectProtectedRouteWithoutTokenReturningStandardJson() throws Exception {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
                        .header("X-Correlation-Id", "corr-missing-token")
        );

        filter.filter(exchange, chain).block();

        assertUnauthorizedError(exchange, "Token ausente", "corr-missing-token");
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldRejectProtectedRouteWithMalformedAuthorizationHeader() throws Exception {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
                        .header("X-Correlation-Id", "corr-malformed-header")
                        .header(HttpHeaders.AUTHORIZATION, "invalid-token")
        );

        filter.filter(exchange, chain).block();

        assertUnauthorizedError(exchange, "Token malformado", "corr-malformed-header");
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldRejectExpiredTokenReturningStandardJson() throws Exception {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        MockServerWebExchange exchange = protectedExchange("expired-token", "corr-expired-token");

        when(jwtUtil.validateToken("expired-token"))
                .thenThrow(new JwtAuthenticationException("Token expirado"));

        filter.filter(exchange, chain).block();

        assertUnauthorizedError(exchange, "Token expirado", "corr-expired-token");
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldRejectMalformedTokenReturningStandardJson() throws Exception {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        MockServerWebExchange exchange = protectedExchange("malformed-token", "corr-malformed-token");

        when(jwtUtil.validateToken("malformed-token"))
                .thenThrow(new JwtAuthenticationException("Token malformado"));

        filter.filter(exchange, chain).block();

        assertUnauthorizedError(exchange, "Token malformado", "corr-malformed-token");
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldRejectInvalidTokenReturningStandardJson() throws Exception {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        MockServerWebExchange exchange = protectedExchange("invalid-token", "corr-invalid-token");

        when(jwtUtil.validateToken("invalid-token"))
                .thenThrow(new JwtAuthenticationException("Token invalido"));

        filter.filter(exchange, chain).block();

        assertUnauthorizedError(exchange, "Token invalido", "corr-invalid-token");
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldGenerateCorrelationIdWhenRejectingWithoutCorrelationHeader() throws Exception {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
        );

        filter.filter(exchange, chain).block();

        JsonNode body = responseBody(exchange);

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getFirst("X-Correlation-Id")).isNotBlank();
        assertThat(body.get("correlationId").asText()).isNotBlank();
        assertThat(body.get("correlationId").asText())
                .isEqualTo(exchange.getResponse().getHeaders().getFirst("X-Correlation-Id"));
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldPropagateUserIdWhenTokenIsValid() {
        Claims claims = mock(Claims.class);
        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
        GatewayFilterChain chain = nextExchange -> {
            capturedExchange.set(nextExchange);
            return Mono.empty();
        };
        MockServerWebExchange exchange = protectedExchange("valid-token", "corr-valid-token");

        when(jwtUtil.validateToken("valid-token")).thenReturn(claims);
        when(jwtUtil.extractUsername(claims)).thenReturn("user-123");

        filter.filter(exchange, chain).block();

        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst("X-User-Id"))
                .isEqualTo("user-123");
        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .isNull();
    }

    @Test
    void shouldOverwriteExternalUserIdWithJwtSubject() {
        Claims claims = mock(Claims.class);
        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
        GatewayFilterChain chain = nextExchange -> {
            capturedExchange.set(nextExchange);
            return Mono.empty();
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
                        .header("X-Correlation-Id", "corr-overwrite-user")
                        .header("X-User-Id", "external-user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
        );

        when(jwtUtil.validateToken("valid-token")).thenReturn(claims);
        when(jwtUtil.extractUsername(claims)).thenReturn("jwt-user");

        filter.filter(exchange, chain).block();

        assertThat(capturedExchange.get().getRequest().getHeaders().get("X-User-Id"))
                .containsExactly("jwt-user");
        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .isNull();
    }

    @Test
    void shouldRejectTokenWithoutUserIdentification() throws Exception {
        Claims claims = mock(Claims.class);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        MockServerWebExchange exchange = protectedExchange("token-without-user", "corr-no-user");

        when(jwtUtil.validateToken("token-without-user")).thenReturn(claims);
        when(jwtUtil.extractUsername(claims)).thenReturn(" ");

        filter.filter(exchange, chain).block();

        assertUnauthorizedError(exchange, "Token sem identificacao do usuario", "corr-no-user");
        verify(chain, never()).filter(any());
    }

    private MockServerWebExchange protectedExchange(String token, String correlationId) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/orders")
                        .header("X-Correlation-Id", correlationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        );
    }

    private PublicRouteProperties publicRouteProperties() {
        PublicRouteProperties properties = new PublicRouteProperties();
        properties.setExact(List.of(
                "/api/v1/auth/login",
                "/api/v1/clients/public/test",
                "/api/v1/orders/public/test",
                "/actuator/health"
        ));
        properties.setPrefixes(List.of("/actuator/health/"));
        return properties;
    }

    private void assertPublicRouteAllowed(String path) {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(path)
        );

        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        verify(jwtUtil, never()).validateToken(any());
    }

    private void assertUnauthorizedError(MockServerWebExchange exchange,
                                         String message,
                                         String correlationId) throws Exception {
        assertUnauthorizedError(exchange, message, correlationId, "/api/v1/orders");
    }

    private void assertUnauthorizedError(MockServerWebExchange exchange,
                                         String message,
                                         String correlationId,
                                         String path) throws Exception {
        JsonNode body = responseBody(exchange);

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(exchange.getResponse().getHeaders().getFirst("X-Correlation-Id")).isEqualTo(correlationId);
        assertThat(body.get("status").asInt()).isEqualTo(401);
        assertThat(body.get("error").asText()).isEqualTo("Unauthorized");
        assertThat(body.get("message").asText()).isEqualTo(message);
        assertThat(body.get("path").asText()).isEqualTo(path);
        assertThat(body.get("correlationId").asText()).isEqualTo(correlationId);
        assertThat(body.get("timestamp").asText()).isNotBlank();
    }

    private JsonNode responseBody(MockServerWebExchange exchange) throws Exception {
        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).isNotBlank();
        return objectMapper.readTree(body);
    }
}
