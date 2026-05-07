package com.jonathanleite.clientapi.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome pode ter no máximo 150 caracteres")
    private String name;

    @NotBlank(message = "Documento é obrigatório")
    @Size(max = 20, message = "Documento pode ter no máximo 20 caracteres")
    private String document;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phone;
}
