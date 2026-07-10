package com.jonathanleite.vitrine.orderservice.dto;

import com.jonathanleite.vitrine.orderservice.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Dados retornados para um pedido")
public class OrderResponseDTO {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "10")
    private Long clientId;

    @Schema(example = "Compra de notebook")
    private String description;

    @Schema(example = "3500.00")
    private BigDecimal amount;

    @Schema(example = "CREATED")
    private OrderStatus status;

    @Schema(example = "2026-07-09T16:30:00")
    private LocalDateTime createdAt;

    @Schema(example = "2026-07-09T16:45:00")
    private LocalDateTime updatedAt;

    public OrderResponseDTO() {
    }

    public OrderResponseDTO(Long id,
                            Long clientId,
                            String description,
                            BigDecimal amount,
                            OrderStatus status,
                            LocalDateTime createdAt,
                            LocalDateTime updatedAt) {
        this.id = id;
        this.clientId = clientId;
        this.description = description;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
