package com.jonathanleite.clientapi.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "ClientPatchRequest",
        description = "Payload usado para atualizar parcialmente um cliente. Campos ausentes são ignorados."
)
public class ClientPatchRequestDTO {

    @Pattern(regexp = ".*\\S.*", message = "Nome não pode ser vazio")
    @Size(max = 150, message = "Nome pode ter no máximo 150 caracteres")
    @Schema(description = "Nome completo do cliente", example = "Maria Souza", maxLength = 150, nullable = true)
    private String name;

    @Pattern(regexp = ".*\\S.*", message = "Email não pode ser vazio")
    @Email(message = "Email inválido")
    @Size(max = 150, message = "Email pode ter no máximo 150 caracteres")
    @Schema(description = "Email único do cliente", example = "maria.souza@email.com", maxLength = 150, nullable = true)
    private String email;

    @Pattern(regexp = "\\d{11}|\\d{14}", message = "Documento deve conter 11 ou 14 dígitos")
    @Schema(description = "CPF com 11 dígitos ou CNPJ com 14 dígitos", example = "12345678900", nullable = true)
    private String document;

    @Pattern(regexp = ".*\\S.*", message = "Telefone não pode ser vazio")
    @Size(max = 20, message = "Telefone pode ter no máximo 20 caracteres")
    @Schema(description = "Telefone de contato do cliente", example = "11888888888", maxLength = 20, nullable = true)
    private String phone;
}
