package com.jonathanleite.vitrine.apigateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "jwt.secret=test-secret-with-at-least-32-characters"
})
class ResilienceConfigurationTest {

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private Environment environment;

    @Test
    void shouldRetryOnlyIdempotentMethods() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions().collectList().block();

        assertRetryOnlyForGet(routeById(routes, "client-api"));
        assertRetryOnlyForGet(routeById(routes, "order-service"));
    }

    @Test
    void shouldConfigureFallbackPerService() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions().collectList().block();

        assertCircuitBreaker(routeById(routes, "client-api"), "clientServiceCircuitBreaker", "forward:/fallback/client");
        assertCircuitBreaker(routeById(routes, "order-service"), "orderServiceCircuitBreaker", "forward:/fallback/order");
    }

    @Test
    void shouldConfigureGatewayHttpTimeouts() {
        assertThat(environment.getProperty("spring.cloud.gateway.httpclient.connect-timeout"))
                .isEqualTo("2000");
        assertThat(environment.getProperty("spring.cloud.gateway.httpclient.response-timeout"))
                .isEqualTo("5s");
    }

    @Test
    void shouldOpenCircuitAfterConfiguredFailures() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("clientServiceCircuitBreaker");

        for (int i = 0; i < 5; i++) {
            circuitBreaker.onError(0, TimeUnit.MILLISECONDS, new IllegalStateException("downstream unavailable"));
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    private void assertRetryOnlyForGet(RouteDefinition routeDefinition) {
        FilterDefinition retryFilter = filterByName(routeDefinition, "Retry");
        Map<String, String> args = retryFilter.getArgs();

        assertThat(args.get("retries")).isEqualTo("2");
        assertThat(args.get("methods")).isEqualTo("GET");
        assertThat(args.get("methods")).doesNotContain("POST", "PATCH", "PUT", "DELETE");
    }

    private void assertCircuitBreaker(RouteDefinition routeDefinition, String name, String fallbackUri) {
        FilterDefinition circuitBreakerFilter = filterByName(routeDefinition, "CircuitBreaker");
        Map<String, String> args = circuitBreakerFilter.getArgs();

        assertThat(args.get("name")).isEqualTo(name);
        assertThat(args.get("fallbackUri")).isEqualTo(fallbackUri);
    }

    private RouteDefinition routeById(List<RouteDefinition> routes, String id) {
        assertThat(routes).isNotNull();
        return routes.stream()
                .filter(route -> id.equals(route.getId()))
                .findFirst()
                .orElseThrow();
    }

    private FilterDefinition filterByName(RouteDefinition routeDefinition, String name) {
        return routeDefinition.getFilters().stream()
                .filter(filter -> name.equals(filter.getName()))
                .findFirst()
                .orElseThrow();
    }
}
