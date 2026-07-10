package com.jonathanleite.vitrine.serviceregistry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("docker")
class ServiceRegistryDockerProfileTests {

	@Autowired
	private Environment environment;

	@Test
	void shouldUseDevelopmentFriendlyEurekaSettingsInDocker() {
		assertThat(environment.getProperty("eureka.client.register-with-eureka")).isEqualTo("false");
		assertThat(environment.getProperty("eureka.client.fetch-registry")).isEqualTo("false");
		assertThat(environment.getProperty("eureka.server.enable-self-preservation")).isEqualTo("false");
		assertThat(environment.getProperty("eureka.server.eviction-interval-timer-in-ms"))
				.isEqualTo("10000");
		assertThat(environment.getProperty("eureka.instance.lease-renewal-interval-in-seconds"))
				.isEqualTo("10");
		assertThat(environment.getProperty("eureka.instance.lease-expiration-duration-in-seconds"))
				.isEqualTo("30");
	}
}
