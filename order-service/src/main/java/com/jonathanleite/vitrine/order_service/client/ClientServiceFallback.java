package com.jonathanleite.vitrine.order_service.client;

import com.jonathanleite.vitrine.order_service.dto.ClientResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ClientServiceFallback implements ClientServiceClient {

    @Override
    public ClientResponseDTO getClientById(Long id) {
        throw new RuntimeException("Client-service indisponível no momento");
    }
}