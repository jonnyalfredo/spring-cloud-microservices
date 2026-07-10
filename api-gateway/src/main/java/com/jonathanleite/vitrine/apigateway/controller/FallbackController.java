package com.jonathanleite.vitrine.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/client")
    public ResponseEntity<String> clientFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("O servico de atendimento ao cliente esta temporariamente indisponivel. Por favor, tente novamente mais tarde.");
    }

    @GetMapping("/order")
    public ResponseEntity<String> orderFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("O servico de pedidos esta temporariamente indisponivel. Tente novamente mais tarde.");
    }
}
