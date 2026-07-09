package com.jonathanleite.vitrine.orderservice.exception;

public class OrderStatusConflictException extends ConflictException {

    public OrderStatusConflictException(String message) {
        super(message);
    }
}
