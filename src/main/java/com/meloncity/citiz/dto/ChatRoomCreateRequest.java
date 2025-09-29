package com.meloncity.citiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 채팅방 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomCreateRequest {
    private String name;
    private String description;
    private Integer maxParticipants;
    private Boolean isPrivate;
}