package com.jonathanleite.vitrine.apigateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonathanleite.vitrine.apigateway.exception.ApiErrorResponse;
import com.jonathanleite.vitrine.apigateway.exception.JwtAuthenticationException;
import com.jonathanleite.vitrine.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public AuthenticationFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Public routes do not require a JWT.
        if (isPublicRoute(path)) {
            return chain.filter(exchange);
        }

        // Protected routes require Authorization: Bearer <token>.
        if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, "Token ausente", HttpStatus.UNAUTHORIZED);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Token malformado", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        Claims claims;

        try {
            claims = jwtUtil.validateToken(token);
        } catch (JwtAuthenticationException e) {
            return onError(exchange, e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return onError(exchange, "Token invalido", HttpStatus.UNAUTHORIZED);
        }

        String username = jwtUtil.extractUsername(claims);

        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header("X-User-Id", username)
                .build();

        return chain.filter(
                exchange.mutate()
                        .request(mutatedRequest)
                        .build()
        );
    }

    private boolean isPublicRoute(String path) {
        return path.contains("/auth") || path.contains("/public");
    }

    private Mono<Void> onError(ServerWebExchange exchange,
                               String err,
                               HttpStatus status) {

        ServerHttpResponse response = exchange.getResponse();
        String correlationId = resolveCorrelationId(exchange);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                LocalDateTime.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                err,
                exchange.getRequest().getURI().getPath(),
                correlationId
        );

        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            body = new byte[0];
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(CORRELATION_ID_HEADER, correlationId);

        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }

    private String resolveCorrelationId(ServerWebExchange exchange) {
        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return correlationId;
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
