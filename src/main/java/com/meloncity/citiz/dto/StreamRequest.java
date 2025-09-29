package com.meloncity.citiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Stream 메시지 추가 요청 DTO
 */
@Setter
@Getter
public class StreamRequest {
    private String streamKey;
    private String content;
    private String sender;
    private Map<String, Object> additionalData;

    public StreamRequest() {}

    public StreamRequest(String streamKey, String content, String sender) {
        this.streamKey = streamKey;
        this.content = content;
        this.sender = sender;
    }

}