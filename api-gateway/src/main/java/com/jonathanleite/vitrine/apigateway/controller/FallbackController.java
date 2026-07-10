package com.jonathanleite.vitrine.apigateway.controller;

import com.jonathanleite.vitrine.apigateway.exception.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @GetMapping("/client")
    public ResponseEntity<ApiErrorResponse> clientFallback(ServerWebExchange exchange) {
        return serviceUnavailable(
                exchange,
                "Client-service indisponivel. Nao foi possivel encaminhar a requisicao no momento."
        );
    }

    @GetMapping("/order")
    public ResponseEntity<ApiErrorResponse> orderFallback(ServerWebExchange exchange) {
        return serviceUnavailable(
                exchange,
                "Order-service indisponivel. Nao foi possivel encaminhar a requisicao no momento."
        );
    }

    private ResponseEntity<ApiErrorResponse> serviceUnavailable(ServerWebExchange exchange, String message) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        String correlationId = resolveCorrelationId(exchange);

        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getURI().getPath(),
                correlationId
        );

        return ResponseEntity
                .status(status)
                .header(CORRELATION_ID_HEADER, correlationId)
                .body(response);
    }

    private String resolveCorrelationId(ServerWebExchange exchange) {
        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return correlationId;
    }
}
