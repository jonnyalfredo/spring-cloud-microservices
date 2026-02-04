package com.jonathanleite.vitrine.customer.dto;

public class CustomerResponseDTO {

    private Long id;
    private String name;
    private String email;
    private Boolean active;

    public CustomerResponseDTO(Long id, String name, String email, Boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getActive() {
        return active;
    }
}
