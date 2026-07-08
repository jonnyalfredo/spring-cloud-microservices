package com.jonathanleite.clientapi.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientPatchRequestDTO {

    private String name;
    private String email;
    private String document;
    private String phone;
}
