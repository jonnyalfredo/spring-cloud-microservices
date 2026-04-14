package com.jonathanleite.vitrine.orderservice.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public class OrderRequestDTO {

    @NotNull(message = "ClientId é obrigatório")
    private Long clientId;

    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    private Double amount;

    public OrderRequestDTO() {
    }

    public OrderRequestDTO(Long clientId, String description, BigDecimal amount) {
        this.clientId = clientId;
        this.description = description;
        this.amount = amount;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}