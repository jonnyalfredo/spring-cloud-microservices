package com.jonathanleite.clientapi.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientPatchRequestDTO {

    @Pattern(regexp = ".*\\S.*", message = "Nome não pode ser vazio")
    @Size(max = 150, message = "Nome pode ter no máximo 150 caracteres")
    private String name;

    @Pattern(regexp = ".*\\S.*", message = "Email não pode ser vazio")
    @Email(message = "Email inválido")
    @Size(max = 150, message = "Email pode ter no máximo 150 caracteres")
    private String email;

    @Pattern(regexp = "\\d{11}|\\d{14}", message = "Documento deve conter 11 ou 14 dígitos")
    private String document;

    @Pattern(regexp = ".*\\S.*", message = "Telefone não pode ser vazio")
    @Size(max = 20, message = "Telefone pode ter no máximo 20 caracteres")
    private String phone;
}
