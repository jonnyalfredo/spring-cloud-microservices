package com.jonathanleite.vitrine.orderservice.controller;

import com.jonathanleite.vitrine.orderservice.dto.OrderRequestDTO;
import com.jonathanleite.vitrine.orderservice.dto.OrderResponseDTO;
import com.jonathanleite.vitrine.orderservice.entity.OrderStatus;
import com.jonathanleite.vitrine.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/public/test")
    public ResponseEntity<String> publicTest() {
        return ResponseEntity.ok("Order API public endpoint OK");
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO request) {
        log.info("Recebida requisição para criar pedido clientId={}", request.getClientId());

        OrderResponseDTO response = orderService.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        log.info("Buscando pedido id={}", id);

        OrderResponseDTO response = orderService.getOrderById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(Pageable pageable) {
        log.info("Listando pedidos page={} size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<OrderResponseDTO> orders = orderService.getAllOrders(pageable);

        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {

        log.info("Atualizando status do pedido id={} para {}", id, status);

        OrderResponseDTO response = orderService.updateStatus(id, status);

        return ResponseEntity.ok(response);
    }
}
