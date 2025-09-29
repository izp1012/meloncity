package com.meloncity.citiz.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 테스트 채팅 메시지 요청 DTO
 */
@Setter
@Getter
public class TestChatRequest {
    // Getters and Setters
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;

    public TestChatRequest() {}

    public TestChatRequest(Long roomId, Long senderId, String senderName, String content) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
    }

}