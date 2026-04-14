package com.jonathanleite.vitrine.order_service.controller;

import com.jonathanleite.vitrine.order_service.dto.OrderRequestDTO;
import com.jonathanleite.vitrine.order_service.dto.OrderResponseDTO;
import com.jonathanleite.vitrine.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@RequestBody @Valid OrderRequestDTO request) {

        OrderResponseDTO response = orderService.createOrder(request);

        return ResponseEntity
                .created(URI.create("/orders/" + response.getId()))
                .body(response);
    }
}