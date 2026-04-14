package com.jonathanleite.vitrine.order_service.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {

    @NotNull
    private Long clientId;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal amount;
}