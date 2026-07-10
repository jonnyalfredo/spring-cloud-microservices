package com.jonathanleite.vitrine.orderservice.client;

import com.jonathanleite.vitrine.orderservice.dto.ClientResponseDTO;
import com.jonathanleite.vitrine.orderservice.exception.BusinessException;
import com.jonathanleite.vitrine.orderservice.exception.ResourceNotFoundException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ClientServiceFallbackFactory implements FallbackFactory<ClientServiceClient> {

    private static final Logger log = LoggerFactory.getLogger(ClientServiceFallbackFactory.class);
    public static final String CLIENT_SERVICE_UNAVAILABLE_MESSAGE =
            "Client-service indisponível. Não foi possível validar o cliente no momento.";

    @Override
    public ClientServiceClient create(Throwable cause) {
        return new ClientServiceClient() {
            @Override
            public ClientResponseDTO getClientById(Long id, String userId) {
                if (cause instanceof FeignException.NotFound) {
                    throw new ResourceNotFoundException("Cliente não encontrado", "CLIENT_NOT_FOUND");
                }

                log.error("Falha ao consultar client-api para clientId={}: {}", id, cause.getMessage());
                throw new BusinessException(
                        CLIENT_SERVICE_UNAVAILABLE_MESSAGE,
                        "CLIENT_SERVICE_UNAVAILABLE",
                        HttpStatus.SERVICE_UNAVAILABLE
                );
            }
        };
    }
}
