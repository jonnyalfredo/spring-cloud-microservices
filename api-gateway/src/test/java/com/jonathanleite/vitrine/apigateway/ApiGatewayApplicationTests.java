package com.jonathanleite.vitrine.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "jwt.secret=test-secret-with-at-least-32-characters"
})
class ApiGatewayApplicationTests {

    @Autowired
    private Environment environment;

    @Test
    void shouldExposeGatewayRoutesConfiguration() {
        assertThat(environment.getProperty("spring.cloud.gateway.routes[0].id"))
                .isEqualTo("client-api");
        assertThat(environment.getProperty("spring.cloud.gateway.routes[1].id"))
                .isEqualTo("order-service");
    }

    @Test
    void shouldRouteBaseAndNestedApiPaths() {
        assertThat(environment.getProperty("spring.cloud.gateway.routes[0].predicates[0]"))
                .isEqualTo("Path=/api/v1/clients,/api/v1/clients/**");
        assertThat(environment.getProperty("spring.cloud.gateway.routes[1].predicates[0]"))
                .isEqualTo("Path=/api/v1/orders,/api/v1/orders/**");
    }

    @Test
    void shouldExposeOnlyRequiredActuatorEndpoints() {
        assertThat(environment.getProperty("management.endpoints.web.exposure.include"))
                .isEqualTo("health,info,metrics,gateway");
        assertThat(environment.getProperty("management.endpoint.health.show-details"))
                .isEqualTo("never");
        assertThat(environment.getProperty("management.endpoint.health.show-components"))
                .isEqualTo("never");
        assertThat(environment.getProperty("management.endpoint.health.probes.enabled"))
                .isEqualTo("true");
        assertThat(environment.getProperty("management.endpoint.gateway.enabled"))
                .isEqualTo("true");
    }

    @Test
    void shouldExposeServiceInformationOnInfoEndpoint() {
        assertThat(environment.getProperty("management.info.env.enabled"))
                .isEqualTo("true");
        assertThat(environment.getProperty("info.app.name"))
                .isEqualTo("api-gateway");
        assertThat(environment.getProperty("info.app.version"))
                .isEqualTo("0.0.1-SNAPSHOT");
    }
}
