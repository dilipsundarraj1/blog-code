package com.webflux.exception;

public class RevenueServerException extends RuntimeException{
    private String message;

    public RevenueServerException(String message) {
        super(message);
        this.message = message;
    }
}
