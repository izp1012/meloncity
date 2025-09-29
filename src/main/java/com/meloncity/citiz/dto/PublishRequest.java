package com.meloncity.citiz.dto;

import lombok.Getter;
import lombok.Setter;

/**
     * Pub/Sub 메시지 발행 요청 DTO
     */
@Setter
@Getter
public class PublishRequest {
    private String message;

    public PublishRequest() {}

    public PublishRequest(String message) {
        this.message = message;
    }

}