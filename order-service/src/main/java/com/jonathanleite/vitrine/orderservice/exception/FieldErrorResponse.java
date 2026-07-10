package com.jonathanleite.vitrine.orderservice.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Detalhe de erro de validação por campo")
public class FieldErrorResponse {

    @Schema(description = "Nome do campo inválido", example = "amount")
    private String field;

    @Schema(description = "Mensagem de validação do campo", example = "O valor deve ser maior que zero")
    private String message;
}
