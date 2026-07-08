package com.jonathanleite.vitrine.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/client")
    public ResponseEntity<String> clientFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("O serviço de atendimento ao cliente está temporariamente indisponível. Por favor, tente novamente mais tarde.");
    }

    @GetMapping("/order")
    public ResponseEntity<String> orderFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("O serviço de pedidos está temporariamente indisponível. Tente novamente mais tarde.");
    }
}