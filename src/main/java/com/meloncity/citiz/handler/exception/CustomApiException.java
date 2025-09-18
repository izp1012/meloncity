package com.meloncity.citiz.handler.exception;

public class CustomApiException extends RuntimeException {
    public CustomApiException() {
    }

    public CustomApiException(String message) {
        super(message);
    }
}
