package com.jonathanleite.vitrine.orderservice.client;

import com.jonathanleite.vitrine.orderservice.dto.ClientResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ClientServiceFallback implements ClientServiceClient {

    @Override
    public ClientResponseDTO getClientById(Long id) {
        // Pode logar ou retornar null para tratar no service
        return null;
    }
}