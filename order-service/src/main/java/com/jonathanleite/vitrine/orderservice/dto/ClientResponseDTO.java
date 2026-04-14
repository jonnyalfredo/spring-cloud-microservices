package com.jonathanleite.vitrine.orderservice.dto;

public class ClientResponseDTO {

    private Long id;
    private String name;
    private String email;
    private boolean active;

    public ClientResponseDTO() {
    }

    public ClientResponseDTO(Long id, String name, String email, boolean active) {
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

    public boolean isActive() {
        return active;
    }
}