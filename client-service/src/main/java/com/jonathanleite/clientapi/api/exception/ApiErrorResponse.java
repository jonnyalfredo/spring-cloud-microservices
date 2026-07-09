package com.jonathanleite.clientapi.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ApiError", description = "Resposta padronizada de erro da API")
public class ApiErrorResponse {

    @Schema(description = "Data e hora em que o erro ocorreu", example = "2026-07-09T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Código HTTP numérico", example = "400")
    private int status;

    @Schema(description = "Descrição padrão do status HTTP", example = "Bad Request")
    private String error;

    @Schema(description = "Mensagem legível do erro", example = "Erro de validação")
    private String message;

    @Schema(description = "Caminho da requisição que gerou o erro", example = "/clients")
    private String path;

    @Schema(description = "Identificador de correlação usado para rastrear a requisição", example = "demo-correlation-id")
    private String correlationId;

    @Schema(description = "Lista de erros de validação por campo")
    private List<FieldErrorResponse> errors;
}
