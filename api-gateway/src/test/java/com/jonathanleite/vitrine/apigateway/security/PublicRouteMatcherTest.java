package com.jonathanleite.vitrine.apigateway.security;

import com.jonathanleite.vitrine.apigateway.config.PublicRouteProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PublicRouteMatcherTest {

    @Test
    void shouldMatchConfiguredExactRoutesAndControlledPrefixes() {
        PublicRouteMatcher matcher = new PublicRouteMatcher(publicRouteProperties());

        assertThat(matcher.isPublic("/api/v1/auth/login")).isTrue();
        assertThat(matcher.isPublic("/api/v1/clients/public/test")).isTrue();
        assertThat(matcher.isPublic("/actuator/health")).isTrue();
        assertThat(matcher.isPublic("/actuator/health/readiness")).isTrue();
    }

    @Test
    void shouldNotMatchRoutesOnlyBecauseTheyContainPublicOrAuth() {
        PublicRouteMatcher matcher = new PublicRouteMatcher(publicRouteProperties());

        assertThat(matcher.isPublic("/api/v1/orders/public-data")).isFalse();
        assertThat(matcher.isPublic("/api/v1/clients/my-auth-history")).isFalse();
        assertThat(matcher.isPublic("/api/v1/publications")).isFalse();
    }

    private PublicRouteProperties publicRouteProperties() {
        PublicRouteProperties properties = new PublicRouteProperties();
        properties.setExact(List.of(
                "/api/v1/auth/login",
                "/api/v1/clients/public/test",
                "/actuator/health"
        ));
        properties.setPrefixes(List.of("/actuator/health/"));
        return properties;
    }
}
