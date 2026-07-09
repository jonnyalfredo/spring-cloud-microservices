package com.jonathanleite.clientapi.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ClientResponse", description = "Dados retornados pela API para representar um cliente")
public class ClientResponseDTO {

    @Schema(description = "Identificador do cliente", example = "1")
    private Long id;

    @Schema(description = "Nome completo do cliente", example = "Maria Silva")
    private String name;

    @Schema(description = "CPF ou CNPJ do cliente", example = "12345678900")
    private String document;

    @Schema(description = "Email do cliente", example = "maria.silva@email.com")
    private String email;

    @Schema(description = "Telefone do cliente", example = "11999999999")
    private String phone;

    @Schema(description = "Indica se o cliente está ativo", example = "true")
    private Boolean active;

    @Schema(description = "Data e hora de criação do cliente", example = "2026-07-09T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data e hora da última atualização do cliente", example = "2026-07-09T10:35:00")
    private LocalDateTime updatedAt;
}
