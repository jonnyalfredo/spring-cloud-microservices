package com.jonathanleite.vitrine.apigateway.security;

import com.jonathanleite.vitrine.apigateway.exception.JwtAuthenticationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class BearerTokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    public String extract(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isBlank()) {
            throw new JwtAuthenticationException("Token ausente");
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            throw new JwtAuthenticationException("Token malformado");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (token.isBlank()) {
            throw new JwtAuthenticationException("Token malformado");
        }

        return token;
    }
}
