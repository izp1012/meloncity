package com.meloncity.citiz.dto;

import com.meloncity.citiz.domain.chat.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageRespDto(
    Long id,
    String senderName,
    String content,
    String status,
    String type,
    LocalDateTime timestamp
) {
    public static ChatMessageRespDto from(ChatMessage chat) {

        return new ChatMessageRespDto(
                chat.getId(),
                chat.getSender().getName(),
                chat.getContent(),
                chat.getStatus().name(),
                chat.getType().name(),
                chat.getCreateDate()
        );
    }
}
