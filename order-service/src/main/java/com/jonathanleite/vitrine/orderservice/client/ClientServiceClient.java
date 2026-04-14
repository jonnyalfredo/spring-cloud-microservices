package com.jonathanleite.vitrine.orderservice.client;

import com.jonathanleite.vitrine.orderservice.dto.ClientResponseDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "client-api",
        path = "/clients",
        fallback = ClientServiceFallback.class
)
public interface ClientServiceClient {

    @GetMapping("/{id}")
    ClientResponseDTO getClientById(@PathVariable("id") Long id);
}