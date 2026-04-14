package com.jonathanleite.vitrine.orderservice.service;

import com.jonathanleite.vitrine.orderservice.entity.Order;
import com.jonathanleite.vitrine.orderservice.entity.OrderStatus;
import com.jonathanleite.vitrine.orderservice.dto.ClientResponseDTO;
import com.jonathanleite.vitrine.orderservice.dto.OrderRequestDTO;
import com.jonathanleite.vitrine.orderservice.dto.OrderResponseDTO;
import com.jonathanleite.vitrine.orderservice.repository.OrderRepository;
import com.jonathanleite.vitrine.orderservice.client.ClientServiceClient;
import com.jonathanleite.vitrine.orderservice.exception.BusinessException;
import com.jonathanleite.vitrine.orderservice.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientServiceClient clientServiceClient;

    public OrderService(OrderRepository orderRepository,
                        ClientServiceClient clientServiceClient) {
        this.orderRepository = orderRepository;
        this.clientServiceClient = clientServiceClient;
    }

    // =========================================================
    // ✅ CRIAR PEDIDO (REGRA PRINCIPAL)
    // =========================================================
    public OrderResponseDTO createOrder(OrderRequestDTO request) {

        // 1. Validar dados básicos
        validateRequest(request);

        // 2. Buscar cliente via Feign
        ClientResponseDTO client = clientServiceClient.getClientById(request.getClientId());

        // 3. Validar cliente
        if (client == null) {
            throw new ResourceNotFoundException("Cliente não encontrado");
        }

        if (!client.isActive()) {
            throw new BusinessException("Cliente está inativo");
        }

        // 4. Criar entidade
        Order order = new Order(
                request.getClientId(),
                request.getDescription(),
                request.getAmount(),
                OrderStatus.CREATED
        );

        // 5. Persistir
        Order savedOrder = orderRepository.save(order);

        // 6. Retornar DTO
        return mapToResponse(savedOrder);
    }

    // =========================================================
    // 🔍 BUSCAR POR ID
    // =========================================================
    public OrderResponseDTO getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        return mapToResponse(order);
    }

    // =========================================================
    // 📋 LISTAR TODOS
    // =========================================================
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // 🔄 ATUALIZAR STATUS
    // =========================================================
    public OrderResponseDTO updateStatus(Long id, OrderStatus status) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        // Regra de negócio simples (pode evoluir depois)
        order.setStatus(status);

        Order updatedOrder = orderRepository.save(order);

        return mapToResponse(updatedOrder);
    }

    // =========================================================
    // ⚠️ VALIDAÇÕES
    // =========================================================
    private void validateRequest(OrderRequestDTO request) {

        if (request.getClientId() == null) {
            throw new BusinessException("ClientId é obrigatório");
        }

        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new BusinessException("Descrição é obrigatória");
        }

        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            throw new BusinessException("Valor deve ser maior que zero");
        }
    }

    // =========================================================
    // 🔄 MAPPER
    // =========================================================
    private OrderResponseDTO mapToResponse(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getClientId(),
                order.getDescription(),
                order.getAmount(),
                order.getStatus()
        );
    }
}