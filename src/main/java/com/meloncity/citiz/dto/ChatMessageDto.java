package com.meloncity.citiz.dto;

import com.meloncity.citiz.domain.chat.ChatStatus;
import com.meloncity.citiz.domain.chat.MessageType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * WebSocket을 통해 주고받는 채팅 메시지 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;
    private MessageType type;
    private ChatStatus status;
    private LocalDateTime timestamp;
    private String streamId;
    // 임시 메시지 ID 추가 (클라이언트에서 생성)
    private String tempId;

    // 실제 메시지 ID (DB 저장 후 생성)
    private Long messageId;

    public static class ChatMessageDtoBuilder {
        private LocalDateTime timestamp = LocalDateTime.now();
        private ChatStatus status = ChatStatus.SENT;
        private MessageType type = MessageType.CHAT;
    }
}