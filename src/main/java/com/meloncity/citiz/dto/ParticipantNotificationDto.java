package com.meloncity.citiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 참여자 입장/퇴장 알림 DTO (Pub/Sub용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantNotificationDto {
    private Long roomId;
    private Long userId;
    private String userName;
    private NotificationType type;
    private LocalDateTime timestamp;
    
    public enum NotificationType {
        JOIN, LEAVE
    }
}