package com.jonathanleite.clientapi.api.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(name = "FieldError", description = "Erro de validação associado a um campo específico")
public class FieldErrorResponse {

    @Schema(description = "Nome do campo inválido", example = "email")
    private String field;

    @Schema(description = "Mensagem de validação", example = "Email inválido")
    private String message;
}
