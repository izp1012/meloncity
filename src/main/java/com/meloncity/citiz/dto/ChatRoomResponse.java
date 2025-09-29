package com.meloncity.citiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅방 정보 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponse {
    private Long id;
    private String name;
    private String description;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer participantCount;
    private Integer maxParticipants;
    private Boolean isPrivate;
    private LocalDateTime timestamp;
}