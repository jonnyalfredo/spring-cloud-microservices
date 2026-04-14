package com.jonathanleite.vitrine.orderservice.dto;

import com.jonathanleite.vitrine.orderservice.entity.OrderStatus;

import java.math.BigDecimal;

public class OrderResponseDTO {

    private Long id;
    private Long clientId;
    private String description;
    private BigDecimal amount;
    private OrderStatus status;

    public OrderResponseDTO() {
    }

    public OrderResponseDTO(Long id, Long clientId, String description, BigDecimal amount, OrderStatus status) {
        this.id = id;
        this.clientId = clientId;
        this.description = description;
        this.amount = amount;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OrderStatus getStatus() {
        return status;
    }
}