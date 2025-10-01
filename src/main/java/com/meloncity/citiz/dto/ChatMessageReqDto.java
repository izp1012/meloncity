package com.meloncity.citiz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.meloncity.citiz.domain.chat.ChatMessage;
import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.domain.chat.ChatRoom;
import com.meloncity.citiz.domain.chat.ChatStatus;
import com.meloncity.citiz.domain.chat.MessageType;

import java.time.LocalDateTime;

public record ChatMessageReqDto(
        @JsonProperty("sender") Long senderId,
        @JsonProperty("content") String content,
        @JsonProperty("type") MessageType type,
        LocalDateTime timestamp) {

    public static ChatMessage toEntity(Profile profile, String content, ChatRoom chatRoom, MessageType type) {
        return ChatMessage.builder()
                .sender(profile)
                .content(content)
                .status(ChatStatus.SENT)
                .type(type)
                .room(chatRoom)
                .build();
    }
}