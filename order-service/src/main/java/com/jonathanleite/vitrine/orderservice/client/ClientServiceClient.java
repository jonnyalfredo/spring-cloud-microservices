package com.jonathanleite.vitrine.orderservice.client;

import com.jonathanleite.vitrine.orderservice.dto.ClientResponseDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "client-api",
        url = "${clients.service.url:}",
        path = "/clients",
        fallbackFactory = ClientServiceFallbackFactory.class
)
public interface ClientServiceClient {

    String SERVICE_USER_ID = "order-service";
    String USER_ID_HEADER = "X-User-Id";

    @GetMapping("/{id}")
    ClientResponseDTO getClientById(
            @PathVariable("id") Long id,
            @RequestHeader(USER_ID_HEADER) String userId
    );
}
