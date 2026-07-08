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

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ClientServiceClient clientServiceClient;

    public OrderService(OrderRepository orderRepository,
                        ClientServiceClient clientServiceClient) {
        this.orderRepository = orderRepository;
        this.clientServiceClient = clientServiceClient;
    }

    // =========================================================
    // ✅ CRIAR PEDIDO
    // =========================================================
    public OrderResponseDTO createOrder(OrderRequestDTO request) {

        log.info("Iniciando criação de pedido para clientId={}", request.getClientId());

        // 1. Validação básica
        validateRequest(request);

        // 2. Buscar cliente (Feign)
        ClientResponseDTO client = getClient(request.getClientId());

        // 3. Validar cliente
        validateClient(client);

        // 4. Criar pedido
        Order order = new Order(
                request.getClientId(),
                request.getDescription(),
                request.getAmount(),
                OrderStatus.CREATED
        );

        Order savedOrder = orderRepository.save(order);

        log.info("Pedido criado com sucesso id={} clientId={} valor={}",
                savedOrder.getId(),
                savedOrder.getClientId(),
                savedOrder.getAmount());

        return mapToResponse(savedOrder);
    }

    // =========================================================
    // 🔍 BUSCAR POR ID
    // =========================================================
    public OrderResponseDTO getOrderById(Long id) {

        log.info("Buscando pedido id={}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Pedido não encontrado id={}", id);
                    return new ResourceNotFoundException("Pedido não encontrado");
                });

        return mapToResponse(order);
    }

    // =========================================================
    // 📋 LISTAR TODOS (COM PAGINAÇÃO)
    // =========================================================
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {

        log.info("Listando pedidos page={} size={}",
                pageable.getPageNumber(),
                pageable.getPageSize());

        return orderRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    // =========================================================
    // 🔄 ATUALIZAR STATUS
    // =========================================================
    public OrderResponseDTO updateStatus(Long id, OrderStatus newStatus) {

        log.info("Atualizando status do pedido id={} para {}", id, newStatus);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        // evita update desnecessário
        if (order.getStatus() == newStatus) {
            throw new BusinessException("Pedido já está com esse status");
        }

        validateStatusTransition(order.getStatus(), newStatus);

        OrderStatus oldStatus = order.getStatus();

        order.setStatus(newStatus);

        Order updatedOrder = orderRepository.save(order);

        log.info("Status atualizado id={} de {} para {}",
                id,
                oldStatus,
                newStatus);

        return mapToResponse(updatedOrder);
    }

    // =========================================================
    // 🔥 MÉTODOS PRIVADOS (REGRAS)
    // =========================================================

    private ClientResponseDTO getClient(Long clientId) {
        try {
            return clientServiceClient.getClientById(clientId, ClientServiceClient.SERVICE_USER_ID);
        } catch (FeignException.NotFound ex) {
            log.warn("Cliente não encontrado no client-api clientId={}", clientId);
            throw new ResourceNotFoundException("Cliente não encontrado", "CLIENT_NOT_FOUND");
        } catch (BusinessException | ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Erro ao chamar client-api para clientId={}: {}", clientId, ex.getMessage());
            throw new BusinessException(
                    "Não foi possível validar o cliente no momento",
                    "CLIENT_SERVICE_UNAVAILABLE",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }

    private void validateClient(ClientResponseDTO client) {

        if (client == null) {
            throw new ResourceNotFoundException("Cliente não encontrado", "CLIENT_NOT_FOUND");
        }

        if (!client.isActive()) {
            throw new BusinessException("Cliente está inativo", "CLIENT_INACTIVE");
        }
    }

    private void validateRequest(OrderRequestDTO request) {

        if (request.getClientId() == null) {
            throw new BusinessException("ClientId é obrigatório");
        }

        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new BusinessException("Descrição é obrigatória");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Valor deve ser maior que zero");
        }
    }

    // REGRA DE TRANSIÇÃO DE STATUS
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {

        if (current == OrderStatus.COMPLETED || current == OrderStatus.CANCELLED) {
            log.warn("Tentativa inválida de alteração de pedido finalizado status={}", current);
            throw new BusinessException("Pedido já finalizado não pode ser alterado");
        }

        if (current == OrderStatus.CREATED && next == OrderStatus.COMPLETED) {
            log.warn("Transição inválida de {} para {}", current, next);
            throw new BusinessException("Pedido deve passar por PROCESSING antes de COMPLETED");
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
