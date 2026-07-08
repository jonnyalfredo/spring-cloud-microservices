package com.jonathanleite.vitrine.serviceregistry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ServiceregistryApplicationTests {

	@Autowired
	private Environment environment;

	@Test
	void shouldRunAsStandaloneEurekaServer() {
		assertThat(environment.getProperty("server.port")).isEqualTo("8761");
		assertThat(environment.getProperty("eureka.client.register-with-eureka")).isEqualTo("false");
		assertThat(environment.getProperty("eureka.client.fetch-registry")).isEqualTo("false");
	}

}
