package com.jonathanleite.clientapi.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ClientRequest",
        description = "Payload usado para criar ou atualizar completamente um cliente"
)
public class ClientRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome pode ter no máximo 150 caracteres")
    @Schema(description = "Nome completo do cliente", example = "Maria Silva", maxLength = 150)
    private String name;

    @NotBlank(message = "Documento é obrigatório")
    @Size(max = 20, message = "Documento pode ter no máximo 20 caracteres")
    @Schema(description = "CPF com 11 dígitos ou CNPJ com 14 dígitos", example = "12345678900", maxLength = 20)
    private String document;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 150)
    @Schema(description = "Email único do cliente", example = "maria.silva@email.com", maxLength = 150)
    private String email;

    @Size(max = 20)
    @Schema(description = "Telefone de contato do cliente", example = "11999999999", maxLength = 20)
    private String phone;
}
