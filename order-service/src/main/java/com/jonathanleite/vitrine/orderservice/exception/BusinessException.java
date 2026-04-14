package com.jonathanleite.vitrine.orderservice.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    // =========================================================
    // CONSTRUTOR PADRÃO
    // =========================================================
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.status = HttpStatus.BAD_REQUEST;
    }

    // =========================================================
    // CONSTRUTOR COM CÓDIGO CUSTOMIZADO
    // =========================================================
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.status = HttpStatus.BAD_REQUEST;
    }

    // =========================================================
    // CONSTRUTOR COMPLETO
    // =========================================================
    public BusinessException(String message, String errorCode, HttpStatus status) {
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