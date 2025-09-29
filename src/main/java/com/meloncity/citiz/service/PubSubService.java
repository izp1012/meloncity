package com.meloncity.citiz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meloncity.citiz.config.redis.RedisPubSubConfig.PubSubChannels;
import com.meloncity.citiz.dto.ParticipantNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

/**
 * Redis Pub/Sub을 이용한 참여자 입장/퇴장 알림 서비스
 * 채팅방 입장/퇴장과 같은 이벤트성 메시지를 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PubSubService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisMessageListenerContainer redisMessageListener;
    private final ObjectMapper objectMapper;

    private final PubSubChannels pubSubChannels;

    /**
     * Pub/Sub 구독 설정을 초기화합니다.
     * 애플리케이션 시작 시 자동으로 실행
     */
    @PostConstruct
    public void initializeSubscriptions() {
        // 입장 알림 채널 구독
        redisMessageListener.addMessageListener((message, pattern) -> {
            Object rawMessage = redisTemplate.getValueSerializer().deserialize(message.getBody());
            if (rawMessage != null) {
                handleNotification(rawMessage.toString(), pubSubChannels.getJoinChannel());
            }
        }, new ChannelTopic(pubSubChannels.getJoinChannel()));

        // 퇴장 알림 채널 구독
        redisMessageListener.addMessageListener((message, pattern) -> {
            Object rawMessage = redisTemplate.getValueSerializer().deserialize(message.getBody());
            if (rawMessage != null) {
                handleNotification(rawMessage.toString(), pubSubChannels.getLeaveChannel());
            }
        }, new ChannelTopic(pubSubChannels.getLeaveChannel()));


        redisMessageListener.addMessageListener((message, pattern) -> {
            Object rawMessage = redisTemplate.getValueSerializer().deserialize(message.getBody());
            if (rawMessage != null) {
                // 일반 알림은 Simple String으로 가정하고 처리
                handleGeneralNotification(rawMessage.toString());
            }
        }, new ChannelTopic(pubSubChannels.getNotificationChannel()));

        log.info("Redis Pub/Sub 구독 설정 완료 - Channels: {}, {}",
                pubSubChannels.getJoinChannel(), pubSubChannels.getLeaveChannel());
    }

    /**
     * 참여자 입장 알림을 발행합니다.
     *
     * @param roomId   채팅방 ID
     * @param userId   입장한 사용자 ID
     * @param userName 입장한 사용자 이름
     */
    public void publishJoinNotification(Long roomId, Long userId, String userName) {
        ParticipantNotificationDto notification = ParticipantNotificationDto.builder()
                .roomId(roomId)
                .userId(userId)
                .userName(userName)
                .type(ParticipantNotificationDto.NotificationType.JOIN)
                .timestamp(LocalDateTime.now())
                .build();

        publishNotification(pubSubChannels.getJoinChannel(), notification);
        log.info("입장 알림 발행 - Room: {}, User: {} ({})", roomId, userName, userId);
    }

    /**
     * 참여자 퇴장 알림을 발행합니다.
     *
     * @param roomId   채팅방 ID
     * @param userId   퇴장한 사용자 ID
     * @param userName 퇴장한 사용자 이름
     */
    public void publishLeaveNotification(Long roomId, Long userId, String userName) {
        ParticipantNotificationDto notification = ParticipantNotificationDto.builder()
                .roomId(roomId)
                .userId(userId)
                .userName(userName)
                .type(ParticipantNotificationDto.NotificationType.LEAVE)
                .timestamp(LocalDateTime.now())
                .build();

        publishNotification(pubSubChannels.getLeaveChannel(), notification);
        log.info("퇴장 알림 발행 - Room: {}, User: {} ({})", roomId, userName, userId);
    }

    /**
     * Redis Pub/Sub 채널에 알림을 발행합니다.
     *
     * @param channel      발행할 채널
     * @param notification 알림 내용
     */
    private void publishNotification(String channel, ParticipantNotificationDto notification) {
        try {
            redisTemplate.convertAndSend(channel, notification);
            log.debug("알림 발행 완료 - Channel: {}, Type: {}, Room: {}",
                    channel, notification.getType(), notification.getRoomId());
        } catch (Exception e) {
            log.error("알림 발행 중 오류 발생 - Channel: {}, Notification: {}", channel, notification, e);
            throw new RuntimeException("Failed to publish notification", e);
        }
    }

    /**
     * 알림 메시지를 처리하고 WebSocket으로 전달합니다. (JOIN/LEAVE 통합 처리)
     * @param message JSON 형태의 알림 메시지
     * @param channel 수신된 채널
     */
    private void handleNotification(String message, String channel) {
        try {
            log.debug("Pub/Sub 메시지 수신 - Channel: {}, Message: {}", channel, message);
            ParticipantNotificationDto notification = objectMapper.readValue(
                    message, ParticipantNotificationDto.class);

            // WebSocket을 통해 채팅방 참여자들에게 알림 전달
            String destination = "/topic/chat/" + notification.getRoomId() + "/participants";
            messagingTemplate.convertAndSend(destination, notification);

            log.info("{} 알림 처리 완료 - Room: {}, User: {}",
                    notification.getType(), notification.getRoomId(), notification.getUserName());

        } catch (Exception e) {
            log.error("알림 처리 중 오류 발생 - Channel: {}, Message: {}", channel, message, e);
        }
    }

    /**
     * 특정 채팅방의 현재 참여자 수를 조회하여 브로드캐스트합니다.
     * @param roomId 채팅방 ID
     * @param currentCount 현재 참여자 수
     */
    public void broadcastParticipantCount(Long roomId, int currentCount) {
        try {
            String destination = "/topic/chat/" + roomId + "/count";
            messagingTemplate.convertAndSend(destination, currentCount);

            log.debug("참여자 수 브로드캐스트 완료 - Room: {}, Count: {}", roomId, currentCount);
        } catch (Exception e) {
            log.error("참여자 수 브로드캐스트 중 오류 발생 - Room: {}", roomId, e);
        }
    }

    /**
     * 일반 알림을 발행합니다.
     * @param roomId 채팅방 ID
     * @param message 알림 메시지
     */
    public void publishGeneralNotification(Long roomId, String message) {
        try {
            // 일반 알림은 단순 문자열로 발행
            redisTemplate.convertAndSend(pubSubChannels.getNotificationChannel(), message);

            log.debug("일반 알림 발행 완료 - Room: {}, Message: {}", roomId, message);
        } catch (Exception e) {
            log.error("일반 알림 발행 중 오류 발생 - Room: {}", roomId, e);
        }
    }

    /**
     * 일반 알림 메시지를 처리합니다. (publishGeneralNotification에 대응)
     * @param message 일반 알림 메시지 (String)
     */
    private void handleGeneralNotification(String message) {
        // 일반 알림은 별도의 DTO 변환 없이 처리 가능
        // 메시지 형식에 따라 알림을 처리하는 로직 추가 필요 (e.g., 메시지에서 roomId 추출)

        // 현재 로직은 roomId를 알 수 없어, 실제 사용 시 message 형식에 roomId 정보가 포함되어야 함
        log.warn("일반 알림 수신 (Room ID 정보 없음). 메시지: {}", message);

        // 예시: message가 "roomId:1,content:새로운 공지" 와 같은 형식일 경우 파싱 필요
        // 현재는 메시지 수신 여부만 로깅하고 넘어가지만, 실제 서비스 로직에 따라 WebSocket 전송 필요
    }
}