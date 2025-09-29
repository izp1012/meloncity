package com.meloncity.citiz.dto;

import com.meloncity.citiz.domain.chat.ChatStatus;
import com.meloncity.citiz.domain.chat.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 히스토리 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageHistoryResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private String content;
    private MessageType type;
    private ChatStatus status;
    private LocalDateTime timestamp;
    private LocalDateTime readAt;
}