package com.jonathanleite.vitrine.orderservice.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    // =========================================================
    // CONSTRUTOR PADRÃO
    // =========================================================
    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = "RESOURCE_NOT_FOUND";
        this.status = HttpStatus.NOT_FOUND;
    }

    // =========================================================
    // CONSTRUTOR COM CÓDIGO CUSTOMIZADO
    // =========================================================
    public ResourceNotFoundException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.status = HttpStatus.NOT_FOUND;
    }

    // =========================================================
    // CONSTRUTOR COMPLETO
    // =========================================================
    public ResourceNotFoundException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    // =========================================================
    // GETTERS
    // =========================================================
    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}