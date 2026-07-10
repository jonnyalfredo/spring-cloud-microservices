package com.jonathanleite.vitrine.serviceregistry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServiceRegistryActuatorEndpointTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldExposeHealthEndpoint() throws Exception {
		ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
		JsonNode body = objectMapper.readTree(response.getBody());

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(body.get("status").asText()).isEqualTo("UP");
		assertThat(body.has("components")).isFalse();
	}

	@Test
	void shouldExposeInfoEndpoint() throws Exception {
		ResponseEntity<String> response = restTemplate.getForEntity("/actuator/info", String.class);
		JsonNode body = objectMapper.readTree(response.getBody());

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(body.path("app").path("name").asText()).isEqualTo("Service Registry");
		assertThat(body.path("app").path("description").asText())
				.isEqualTo("Eureka Server responsavel pelo service discovery");
		assertThat(body.path("app").path("version").asText()).isEqualTo("1.0.0");
	}

	@Test
	void shouldNotExposeSensitiveActuatorEndpoints() {
		ResponseEntity<String> response = restTemplate.getForEntity("/actuator/env", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
}
