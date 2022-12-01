package com.webflux.exception;

public class RevenueClientException extends RuntimeException{
    private String message;

    public RevenueClientException(String message) {
        super(message);
        this.message = message;
    }
}
