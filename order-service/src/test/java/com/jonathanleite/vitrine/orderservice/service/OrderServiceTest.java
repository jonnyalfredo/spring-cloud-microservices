package com.jonathanleite.vitrine.orderservice.service;

import com.jonathanleite.vitrine.orderservice.client.ClientServiceClient;
import com.jonathanleite.vitrine.orderservice.dto.ClientResponseDTO;
import com.jonathanleite.vitrine.orderservice.dto.OrderRequestDTO;
import com.jonathanleite.vitrine.orderservice.dto.OrderResponseDTO;
import com.jonathanleite.vitrine.orderservice.entity.Order;
import com.jonathanleite.vitrine.orderservice.entity.OrderStatus;
import com.jonathanleite.vitrine.orderservice.exception.BusinessException;
import com.jonathanleite.vitrine.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientServiceClient clientServiceClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrderForActiveClient() {
        OrderRequestDTO request = new OrderRequestDTO(1L, "Compra teste", BigDecimal.TEN);
        ClientResponseDTO client = new ClientResponseDTO(1L, "Jonathan", "jonathan@email.com", true);

        when(clientServiceClient.getClientById(1L)).thenReturn(client);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = orderService.createOrder(request);

        assertEquals(1L, response.getClientId());
        assertEquals(OrderStatus.CREATED, response.getStatus());
        assertEquals(BigDecimal.TEN, response.getAmount());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldRejectInactiveClient() {
        OrderRequestDTO request = new OrderRequestDTO(1L, "Compra teste", BigDecimal.TEN);
        ClientResponseDTO inactiveClient = new ClientResponseDTO(1L, "Jonathan", "jonathan@email.com", false);

        when(clientServiceClient.getClientById(1L)).thenReturn(inactiveClient);

        assertThrows(BusinessException.class, () -> orderService.createOrder(request));
    }

    @Test
    void shouldRejectInvalidAmount() {
        OrderRequestDTO request = new OrderRequestDTO(1L, "Compra teste", BigDecimal.ZERO);

        assertThrows(BusinessException.class, () -> orderService.createOrder(request));
    }

    @Test
    void shouldUpdateStatusFromCreatedToProcessing() {
        Order order = new Order(1L, "Compra teste", BigDecimal.TEN, OrderStatus.CREATED);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = orderService.updateStatus(10L, OrderStatus.PROCESSING);

        assertEquals(OrderStatus.PROCESSING, response.getStatus());
    }

    @Test
    void shouldRejectInvalidStatusTransitionFromCreatedToCompleted() {
        Order order = new Order(1L, "Compra teste", BigDecimal.TEN, OrderStatus.CREATED);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> orderService.updateStatus(10L, OrderStatus.COMPLETED));
    }
}
