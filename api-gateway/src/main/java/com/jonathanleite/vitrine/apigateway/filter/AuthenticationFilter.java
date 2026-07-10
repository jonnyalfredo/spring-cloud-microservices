package com.jonathanleite.vitrine.apigateway.filter;

import com.jonathanleite.vitrine.apigateway.exception.JwtAuthenticationException;
import com.jonathanleite.vitrine.apigateway.security.AuthenticationErrorWriter;
import com.jonathanleite.vitrine.apigateway.security.BearerTokenExtractor;
import com.jonathanleite.vitrine.apigateway.security.JwtAuthenticationService;
import com.jonathanleite.vitrine.apigateway.security.PublicRouteMatcher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final PublicRouteMatcher publicRouteMatcher;
    private final BearerTokenExtractor bearerTokenExtractor;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final AuthenticationErrorWriter authenticationErrorWriter;

    public AuthenticationFilter(PublicRouteMatcher publicRouteMatcher,
                                BearerTokenExtractor bearerTokenExtractor,
                                JwtAuthenticationService jwtAuthenticationService,
                                AuthenticationErrorWriter authenticationErrorWriter) {
        this.publicRouteMatcher = publicRouteMatcher;
        this.bearerTokenExtractor = bearerTokenExtractor;
        this.jwtAuthenticationService = jwtAuthenticationService;
        this.authenticationErrorWriter = authenticationErrorWriter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Public routes do not require a JWT.
        if (publicRouteMatcher.isPublic(path)) {
            return chain.filter(exchange);
        }

        try {
            String token = bearerTokenExtractor.extract(exchange.getRequest());
            String username = jwtAuthenticationService.authenticate(token);
            return chain.filter(withUserId(exchange, username));
        } catch (JwtAuthenticationException e) {
            return authenticationErrorWriter.write(exchange, e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private ServerWebExchange withUserId(ServerWebExchange exchange, String username) {
        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .headers(headers -> {
                    headers.remove(HttpHeaders.AUTHORIZATION);
                    headers.remove(USER_ID_HEADER);
                    headers.set(USER_ID_HEADER, username);
                })
                .build();

        return exchange.mutate().request(mutatedRequest).build();
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
