package com.meloncity.citiz.dto;

import com.meloncity.citiz.domain.chat.ChatRoom;

import java.time.LocalDateTime;

public record ChatRoomRespDto(
        Long id,
        String lastMessage,
        LocalDateTime lastMessageTime
) {
    public static ChatRoomRespDto from(ChatRoom chatRoom) {
        return new ChatRoomRespDto(
                chatRoom.getId(),
                chatRoom.getLastMessage(),
                chatRoom.getLastMessageTime()
        );
    }
}