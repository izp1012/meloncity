package com.meloncity.citiz.handler.exception;

import org.springframework.http.HttpStatus;

public class CustomApiException extends RuntimeException {
    private final HttpStatus status;

    public CustomApiException(HttpStatus httpStatus, String message) {
        super(message); this.status = httpStatus;
    }

    public HttpStatus getStatus() {return status;}
}
