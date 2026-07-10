package com.jonathanleite.vitrine.orderservice.client;

import com.jonathanleite.vitrine.orderservice.exception.BusinessException;
import com.jonathanleite.vitrine.orderservice.exception.ResourceNotFoundException;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientServiceFallbackFactoryTest {

    private final ClientServiceFallbackFactory fallbackFactory = new ClientServiceFallbackFactory();

    @Test
    void shouldMapFeignNotFoundToClientNotFound() {
        ClientServiceClient fallback = fallbackFactory.create(notFoundException());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> fallback.getClientById(99L, ClientServiceClient.SERVICE_USER_ID)
        );

        assertEquals("CLIENT_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void shouldMapUnavailableClientServiceToClearBusinessError() {
        ClientServiceClient fallback = fallbackFactory.create(new RuntimeException("timeout"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> fallback.getClientById(1L, ClientServiceClient.SERVICE_USER_ID)
        );

        assertEquals("CLIENT_SERVICE_UNAVAILABLE", exception.getErrorCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
        assertEquals(ClientServiceFallbackFactory.CLIENT_SERVICE_UNAVAILABLE_MESSAGE, exception.getMessage());
    }

    private FeignException.NotFound notFoundException() {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/clients/99",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                null
        );

        return new FeignException.NotFound("not found", request, null, Collections.emptyMap());
    }
}
