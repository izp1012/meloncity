package com.meloncity.citiz.handler.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomApiException extends RuntimeException{
    private final HttpStatus status;

    public CustomApiException(HttpStatus httpStatus, String message) {
        super(message);
        this.status = httpStatus;
    }

}
