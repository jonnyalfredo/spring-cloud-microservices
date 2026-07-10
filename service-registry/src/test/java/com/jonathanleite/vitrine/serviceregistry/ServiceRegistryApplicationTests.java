package com.jonathanleite.vitrine.serviceregistry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ServiceRegistryApplicationTests {

	@Autowired
	private Environment environment;

	@Test
	void shouldRunAsStandaloneEurekaServer() {
		assertThat(environment.getProperty("server.port")).isEqualTo("8761");
		assertThat(environment.getProperty("eureka.client.register-with-eureka")).isEqualTo("false");
		assertThat(environment.getProperty("eureka.client.fetch-registry")).isEqualTo("false");
		assertThat(environment.getProperty("eureka.server.enable-self-preservation")).isEqualTo("false");
	}

	@Test
	void shouldUseDevelopmentFriendlyEurekaIntervalsLocally() {
		assertThat(environment.getProperty("eureka.server.eviction-interval-timer-in-ms"))
				.isEqualTo("10000");
		assertThat(environment.getProperty("eureka.instance.lease-renewal-interval-in-seconds"))
				.isEqualTo("10");
		assertThat(environment.getProperty("eureka.instance.lease-expiration-duration-in-seconds"))
				.isEqualTo("30");
	}

	@Test
	void shouldExposeOnlyRequiredActuatorEndpoints() {
		assertThat(environment.getProperty("management.endpoints.web.exposure.include"))
				.isEqualTo("health,info");
		assertThat(environment.getProperty("management.info.env.enabled"))
				.isEqualTo("true");
		assertThat(environment.getProperty("management.endpoint.health.show-details"))
				.isEqualTo("never");
		assertThat(environment.getProperty("management.endpoint.health.show-components"))
				.isEqualTo("never");
	}

	@Test
	void shouldExposeServiceRegistryInformation() {
		assertThat(environment.getProperty("info.app.name"))
				.isEqualTo("Service Registry");
		assertThat(environment.getProperty("info.app.description"))
				.isEqualTo("Eureka Server responsavel pelo service discovery");
		assertThat(environment.getProperty("info.app.version"))
				.isEqualTo("1.0.0");
	}

}
