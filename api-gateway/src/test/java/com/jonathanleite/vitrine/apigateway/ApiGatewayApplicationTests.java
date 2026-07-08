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
}
