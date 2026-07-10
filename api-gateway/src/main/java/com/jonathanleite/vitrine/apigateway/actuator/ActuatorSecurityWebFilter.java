package com.jonathanleite.vitrine.apigateway.actuator;

import com.jonathanleite.vitrine.apigateway.exception.JwtAuthenticationException;
import com.jonathanleite.vitrine.apigateway.security.AuthenticationErrorWriter;
import com.jonathanleite.vitrine.apigateway.security.BearerTokenExtractor;
import com.jonathanleite.vitrine.apigateway.security.JwtAuthenticationService;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class ActuatorSecurityWebFilter implements WebFilter, Ordered {

    private static final String ACTUATOR_ROOT = "/actuator";

    private static final List<String> PROTECTED_ACTUATOR_PATHS = List.of(
            "/actuator/info",
            "/actuator/metrics",
            "/actuator/gateway"
    );

    private final BearerTokenExtractor bearerTokenExtractor;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final AuthenticationErrorWriter authenticationErrorWriter;

    public ActuatorSecurityWebFilter(BearerTokenExtractor bearerTokenExtractor,
                                     JwtAuthenticationService jwtAuthenticationService,
                                     AuthenticationErrorWriter authenticationErrorWriter) {
        this.bearerTokenExtractor = bearerTokenExtractor;
        this.jwtAuthenticationService = jwtAuthenticationService;
        this.authenticationErrorWriter = authenticationErrorWriter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!isProtectedActuatorPath(exchange.getRequest().getURI().getPath())) {
            return chain.filter(exchange);
        }

        try {
            String token = bearerTokenExtractor.extract(exchange.getRequest());
            jwtAuthenticationService.authenticate(token);
            return chain.filter(exchange);
        } catch (JwtAuthenticationException e) {
            return authenticationErrorWriter.write(exchange, e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isProtectedActuatorPath(String path) {
        return ACTUATOR_ROOT.equals(path)
                || PROTECTED_ACTUATOR_PATHS.stream().anyMatch(protectedPath ->
                path.equals(protectedPath) || path.startsWith(protectedPath + "/"));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
