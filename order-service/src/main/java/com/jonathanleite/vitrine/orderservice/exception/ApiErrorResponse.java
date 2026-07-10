package com.jonathanleite.vitrine.orderservice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta padronizada de erro da API")
public class ApiErrorResponse {

    @Schema(description = "Data e hora em que o erro ocorreu", example = "2026-07-09T16:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Código HTTP retornado", example = "400")
    private int status;

    @Schema(description = "Descrição padrão do status HTTP", example = "Bad Request")
    private String error;

    @Schema(description = "Mensagem de erro legível para o consumidor", example = "Erro de validação")
    private String message;

    @Schema(description = "Caminho chamado pelo consumidor", example = "/orders")
    private String path;

    @Schema(description = "Identificador de correlação da requisição", example = "demo-order-001")
    private String correlationId;

    @Schema(description = "Erros de campo retornados em falhas de validação")
    private List<FieldErrorResponse> errors;
}
