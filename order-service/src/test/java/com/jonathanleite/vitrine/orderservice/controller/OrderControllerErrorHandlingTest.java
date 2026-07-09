package com.jonathanleite.vitrine.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonathanleite.vitrine.orderservice.dto.OrderRequestDTO;
import com.jonathanleite.vitrine.orderservice.dto.OrderResponseDTO;
import com.jonathanleite.vitrine.orderservice.entity.OrderStatus;
import com.jonathanleite.vitrine.orderservice.exception.BusinessException;
import com.jonathanleite.vitrine.orderservice.exception.ConflictException;
import com.jonathanleite.vitrine.orderservice.exception.ForbiddenException;
import com.jonathanleite.vitrine.orderservice.exception.OrderStatusConflictException;
import com.jonathanleite.vitrine.orderservice.exception.ResourceNotFoundException;
import com.jonathanleite.vitrine.orderservice.exception.UnauthorizedException;
import com.jonathanleite.vitrine.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnCreatedAndUpdatedDatesWhenOrderIsCreated() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO(1L, "Compra teste", BigDecimal.TEN);
        OrderResponseDTO response = new OrderResponseDTO(
                10L,
                1L,
                "Compra teste",
                BigDecimal.TEN,
                OrderStatus.CREATED,
                LocalDateTime.of(2026, 7, 9, 16, 30),
                LocalDateTime.of(2026, 7, 9, 16, 30)
        );

        when(orderService.createOrder(any(OrderRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.createdAt").value("2026-07-09T16:30:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-07-09T16:30:00"));
    }

    @Test
    void shouldReturn400WithFieldErrorsWhenRequestIsInvalid() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO(null, "", BigDecimal.ZERO);

        mockMvc.perform(post("/orders")
                        .header("X-Correlation-Id", "corr-order-validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-Id", "corr-order-validation"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.path").value("/orders"))
                .andExpect(jsonPath("$.correlationId").value("corr-order-validation"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").exists())
                .andExpect(jsonPath("$.errors[0].message").exists());
    }

    @Test
    void shouldReturn400WhenBusinessRuleFails() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO(1L, "Compra teste", BigDecimal.TEN);

        when(orderService.createOrder(any(OrderRequestDTO.class)))
                .thenThrow(new BusinessException("Cliente está inativo"));

        mockMvc.perform(post("/orders")
                        .header("X-Correlation-Id", "corr-order-400")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-Id", "corr-order-400"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Cliente está inativo"))
                .andExpect(jsonPath("$.path").value("/orders"))
                .andExpect(jsonPath("$.correlationId").value("corr-order-400"));
    }

    @Test
    void shouldReturn400WhenRequiredRequestParamIsMissing() throws Exception {
        mockMvc.perform(patch("/orders/{id}/status", 1L)
                        .header("X-Correlation-Id", "corr-order-param"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-Id", "corr-order-param"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Parâmetro obrigatório ausente: status"))
                .andExpect(jsonPath("$.path").value("/orders/1/status"))
                .andExpect(jsonPath("$.correlationId").value("corr-order-param"));
    }

    @Test
    void shouldReturn404WhenOrderIsNotFound() throws Exception {
        when(orderService.getOrderById(99L))
                .thenThrow(new ResourceNotFoundException("Pedido não encontrado"));

        mockMvc.perform(get("/orders/{id}", 99L)
                        .header("X-Correlation-Id", "corr-order-404"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-Correlation-Id", "corr-order-404"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Pedido não encontrado"))
                .andExpect(jsonPath("$.path").value("/orders/99"))
                .andExpect(jsonPath("$.correlationId").value("corr-order-404"));
    }

    @Test
    void shouldReturn409WhenConflictHappens() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO(1L, "Compra teste", BigDecimal.TEN);

        when(orderService.createOrder(any(OrderRequestDTO.class)))
                .thenThrow(new ConflictException("Pedido duplicado"));

        mockMvc.perform(post("/orders")
                        .header("X-Correlation-Id", "corr-order-409")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(header().string("X-Correlation-Id", "corr-order-409"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Pedido duplicado"))
                .andExpect(jsonPath("$.path").value("/orders"))
                .andExpect(jsonPath("$.correlationId").value("corr-order-409"));
    }

    @Test
    void shouldReturn409WhenOrderStatusConflictHappens() throws Exception {
        when(orderService.updateStatus(1L, com.jonathanleite.vitrine.orderservice.entity.OrderStatus.COMPLETED))
                .thenThrow(new OrderStatusConflictException("Transição inválida de CREATED para COMPLETED"));

        mockMvc.perform(patch("/orders/{id}/status", 1L)
                        .param("status", "COMPLETED")
                        .header("X-Correlation-Id", "corr-order-status-409"))
                .andExpect(status().isConflict())
                .andExpect(header().string("X-Correlation-Id", "corr-order-status-409"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Transição inválida de CREATED para COMPLETED"))
                .andExpect(jsonPath("$.path").value("/orders/1/status"))
                .andExpect(jsonPath("$.correlationId").value("corr-order-status-409"));
    }

    @Test
    void shouldReturn401WhenUnauthorized() throws Exception {
        when(orderService.getOrderById(1L)).thenThrow(new UnauthorizedException("Não autorizado"));

        mockMvc.perform(get("/orders/{id}", 1L)
                        .header("X-Correlation-Id", "corr-order-401"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Correlation-Id", "corr-order-401"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Não autorizado"))
                .andExpect(jsonPath("$.path").value("/orders/1"))
                .andExpect(jsonPath("$.correlationId").value("corr-order-401"));
    }

    @Test
    void shouldReturn403WhenForbidden() throws Exception {
        when(orderService.getOrderById(1L)).thenThrow(new ForbiddenException("Acesso negado"));

        mockMvc.perform(get("/orders/{id}", 1L)
                        .header("X-Correlation-Id", "corr-order-403"))
                .andExpect(status().isForbidden())
                .andExpect(header().string("X-Correlation-Id", "corr-order-403"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Acesso negado"))
                .andExpect(jsonPath("$.path").value("/orders/1"))
                .andExpect(jsonPath("$.correlationId").value("corr-order-403"));
    }

    @Test
    void shouldReturn500WhenUnexpectedErrorHappens() throws Exception {
        when(orderService.getOrderById(1L)).thenThrow(new RuntimeException("Falha inesperada"));

        mockMvc.perform(get("/orders/{id}", 1L)
                        .header("X-Correlation-Id", "corr-order-500"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string("X-Correlation-Id", "corr-order-500"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Erro interno inesperado"))
                .andExpect(jsonPath("$.path").value("/orders/1"))
                .andExpect(jsonPath("$.correlationId").value("corr-order-500"));
    }
}
