package com.meloncity.citiz.controller;

import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.domain.chat.MessageType;
import com.meloncity.citiz.dto.ChatMessageDto;
import com.meloncity.citiz.dto.ProfileRespDto;
import com.meloncity.citiz.service.ChatService;
import com.meloncity.citiz.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

/**
 * WebSocket을 통한 실시간 채팅 메시지를 처리하는 컨트롤러
 * STOMP 프로토콜을 사용하여 클라이언트와 실시간 통신
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

    private final ChatService chatService;
    private final ProfileService profileService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 채팅 메시지를 처리합니다.
     * 클라이언트에서 /app/chat/send/{roomId}로 메시지를 보내면 처리
     *
     * @param roomId 채팅방 ID
     * @param messageDto 전송할 메시지 정보
     * @param userId 사용자 ID (헤더에서 추출)
     * @param headerAccessor WebSocket 세션 헤더 정보
     */
    @MessageMapping("/chat/send/{roomId}")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload ChatMessageDto messageDto,
            @Header("User-Id") Long userId, // 헤더에서 사용자 ID 추출
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();

        try {
            // 메시지 기본 정보 설정
            messageDto.setRoomId(roomId);
            messageDto.setSenderId(userId);


            // 임시 ID가 없으면 생성
            if (messageDto.getTempId() == null || messageDto.getTempId().trim().isEmpty()) {
                messageDto.setTempId(UUID.randomUUID().toString());
            }

            log.info("채팅 메시지 수신 - Room: {}, Temp ID: {}, Sender: {}",
                    roomId, messageDto.getTempId(), messageDto.getSenderId());

            // 메시지 타입 및 상태 설정
            if (messageDto.getType() == null) {
                messageDto.setType(MessageType.CHAT);
            }

            // 발신자 이름 설정
            if (messageDto.getSenderName() == null || messageDto.getSenderName().trim().isEmpty()) {
                messageDto.setSenderName("User_" + messageDto.getSenderId());
            }

            // 발신자 정보 설정
//            setMessageSenderInfo(messageDto, userId);

            // 메시지 유효성 검증
            validateMessageContent(messageDto);

            // Redis Stream에 메시지 발행
            String streamId = chatService.publishMessage(messageDto);

            log.info("채팅 메시지 Stream 발행 완료 - Room: {}, Stream ID: {}, Sender: {}",
                    roomId, streamId, userId);

        } catch (IllegalArgumentException e) {
            log.warn("채팅 메시지 처리 실패 - Room: {}, Sender: {}, Reason: {}",
                    roomId, userId, e.getMessage());

            // 클라이언트에게 오류 메시지 전송
            sendErrorMessage(roomId, sessionId, e.getMessage());

        } catch (Exception e) {
            log.error("채팅 메시지 처리 중 예상치 못한 오류 발생 - Room: {}, Sender: {}",
                    roomId, userId, e);

            // 클라이언트에게 일반적인 오류 메시지 전송
            sendErrorMessage(roomId, sessionId, "메시지 전송 중 오류가 발생했습니다.");
        }
    }

    /**
     * 메시지에 발신자 정보를 설정합니다.
     * @param messageDto 메시지 DTO
     * @param userId 사용자 ID
     */
    private void setMessageSenderInfo(ChatMessageDto messageDto, Long userId) {
        try {
            Profile profile = profileService.findById(userId);
            ProfileRespDto profileRespDto = ProfileRespDto.from(profile);
            messageDto.setSenderName(profileRespDto.name());

            log.debug("발신자 정보 설정 완료 - User: {}, Name: {}", userId, profileRespDto.name());
        } catch (Exception e) {
            log.warn("발신자 정보 조회 실패, 기본값 사용 - User: {}", userId);
            messageDto.setSenderName("User_" + userId);
        }
    }

    /**
     * 메시지 내용의 유효성을 검증합니다.
     * @param messageDto 검증할 메시지
     * @throws IllegalArgumentException 유효하지 않은 메시지인 경우
     */
    private void validateMessageContent(ChatMessageDto messageDto) {
        if (messageDto.getContent() == null || messageDto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용이 비어있습니다.");
        }

        if (messageDto.getContent().length() > 1000) {
            throw new IllegalArgumentException("메시지가 너무 깁니다. (최대 1000자)");
        }

        // 스팸 방지를 위한 추가 검증
        if (messageDto.getContent().matches(".*[\\p{Sc}]{10,}.*")) { // 특수문자 반복 체크
            throw new IllegalArgumentException("부적절한 메시지 형식입니다.");
        }
    }

    /**
     * 클라이언트에게 오류 메시지를 전송합니다.
     * @param roomId 채팅방 ID
     * @param sessionId 세션 ID
     * @param errorMessage 오류 메시지
     */
    private void sendErrorMessage(Long roomId, String sessionId, String errorMessage) {
        try {
            String destination = "/queue/errors-" + sessionId;
            messagingTemplate.convertAndSend(destination, Map.of(
                    "type", "ERROR",
                    "roomId", roomId,
                    "message", errorMessage,
                    "timestamp", System.currentTimeMillis()
            ));

            log.debug("오류 메시지 전송 완료 - Session: {}, Error: {}", sessionId, errorMessage);
        } catch (Exception e) {
            log.error("오류 메시지 전송 실패 - Session: {}", sessionId, e);
        }
    }

    /**
     * 사용자가 채팅방에 연결되었을 때 처리합니다.
     * 클라이언트에서 /app/chat/connect/{roomId}로 연결 메시지를 보내면 처리
     *
     * @param roomId 연결할 채팅방 ID
     * @param userId 사용자 ID
     * @param headerAccessor WebSocket 세션 헤더 정보
     */
    @MessageMapping("/chat/connect/{roomId}")
    public void connectToRoom(
            @DestinationVariable Long roomId,
            @Header("User-Id") Long userId,
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();
        log.info("채팅방 연결 요청 - Room: {}, User: {}, Session: {}", roomId, userId, sessionId);

        try {
            // WebSocket 세션에 채팅방 정보 저장
            headerAccessor.getSessionAttributes().put("roomId", roomId);
            headerAccessor.getSessionAttributes().put("userId", userId);

            // 채팅방 참여 권한 검증
            validateRoomAccess(roomId, userId);

            // 온라인 사용자 목록에 추가 (Redis)
            addToOnlineUsers(roomId, userId);

            // 연결 성공 메시지 전송
            sendConnectionSuccessMessage(roomId, sessionId);

            log.info("채팅방 연결 완료 - Room: {}, User: {}, Session: {}", roomId, userId, sessionId);

        } catch (Exception e) {
            log.error("채팅방 연결 중 오류 발생 - Room: {}, User: {}, Session: {}", roomId, userId, sessionId, e);
            sendErrorMessage(roomId, sessionId, "채팅방 연결에 실패했습니다.");
        }
    }

    /**
     * 사용자가 채팅방에서 연결을 해제할 때 처리합니다.
     * 클라이언트에서 /app/chat/disconnect/{roomId}로 연결 해제 메시지를 보내면 처리
     *
     * @param roomId 연결을 해제할 채팅방 ID
     * @param userId 사용자 ID
     * @param headerAccessor WebSocket 세션 헤더 정보
     */
    @MessageMapping("/chat/disconnect/{roomId}")
    public void disconnectFromRoom(
            @DestinationVariable Long roomId,
            @Header("User-Id") Long userId,
            SimpMessageHeaderAccessor headerAccessor) {

        String sessionId = headerAccessor.getSessionId();
        log.info("채팅방 연결 해제 요청 - Room: {}, User: {}, Session: {}", roomId, userId, sessionId);

        try {
            // WebSocket 세션에서 채팅방 정보 제거
            headerAccessor.getSessionAttributes().remove("roomId");
            headerAccessor.getSessionAttributes().remove("userId");

            // 온라인 사용자 목록에서 제거 (Redis)
            removeFromOnlineUsers(roomId, userId);

            log.info("채팅방 연결 해제 완료 - Room: {}, User: {}, Session: {}", roomId, userId, sessionId);

        } catch (Exception e) {
            log.error("채팅방 연결 해제 중 오류 발생 - Room: {}, User: {}, Session: {}", roomId, userId, sessionId, e);
        }
    }

    /**
     * 사용자의 타이핑 상태를 처리합니다.
     * 클라이언트에서 /app/chat/typing/{roomId}로 타이핑 상태를 보내면 처리
     *
     * @param roomId 채팅방 ID
     * @param typingInfo 타이핑 정보
     * @param userId 사용자 ID
     * @param headerAccessor WebSocket 세션 헤더 정보
     */
    @MessageMapping("/chat/typing/{roomId}")
    public void handleTyping(
            @DestinationVariable Long roomId,
            @Payload TypingInfo typingInfo,
            @Header("User-Id") Long userId,
            SimpMessageHeaderAccessor headerAccessor) {

        try {
            // 타이핑 정보 유효성 검증
            typingInfo.setUserId(userId);

            // 사용자 이름 설정
            if (typingInfo.getUserName() == null) {
                try {
                    Profile profile = profileService.findById(userId);
                    ProfileRespDto profileRespDto = ProfileRespDto.from(profile);
                    typingInfo.setUserName(profileRespDto.name());
                } catch (Exception e) {
                    typingInfo.setUserName("User_" + userId);
                }
            }

            log.debug("타이핑 상태 수신 - Room: {}, User: {}, Typing: {}",
                    roomId, userId, typingInfo.isTyping());

            // 다른 참여자들에게 타이핑 상태 브로드캐스트
            String destination = "/topic/chat/" + roomId + "/typing";
            messagingTemplate.convertAndSend(destination, typingInfo);

        } catch (Exception e) {
            log.error("타이핑 상태 처리 중 오류 발생 - Room: {}, User: {}", roomId, userId, e);
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * 채팅방 접근 권한을 검증합니다.
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @throws IllegalArgumentException 권한이 없는 경우
     */
    private void validateRoomAccess(Long roomId, Long userId) {
        // TODO: 실제 권한 검증 로직 구현
        // 예: chatService.hasRoomAccess(roomId, userId)
        log.debug("채팅방 접근 권한 검증 - Room: {}, User: {}", roomId, userId);
    }

    /**
     * 온라인 사용자 목록에 사용자를 추가합니다.
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    private void addToOnlineUsers(Long roomId, Long userId) {
        // TODO: Redis를 통한 온라인 사용자 관리
        // redisService.addOnlineUser(roomId, userId);
        log.debug("온라인 사용자 추가 - Room: {}, User: {}", roomId, userId);
    }

    /**
     * 온라인 사용자 목록에서 사용자를 제거합니다.
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    private void removeFromOnlineUsers(Long roomId, Long userId) {
        // TODO: Redis를 통한 온라인 사용자 관리
        // redisService.removeOnlineUser(roomId, userId);
        log.debug("온라인 사용자 제거 - Room: {}, User: {}", roomId, userId);
    }

    /**
     * 연결 성공 메시지를 전송합니다.
     * @param roomId 채팅방 ID
     * @param sessionId 세션 ID
     */
    private void sendConnectionSuccessMessage(Long roomId, String sessionId) {
        try {
            String destination = "/queue/connect-" + sessionId;
            messagingTemplate.convertAndSend(destination, Map.of(
                    "type", "CONNECTION_SUCCESS",
                    "roomId", roomId,
                    "message", "채팅방에 성공적으로 연결되었습니다.",
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("연결 성공 메시지 전송 실패 - Session: {}", sessionId, e);
        }
    }

    /**
     * 타이핑 정보를 담는 내부 클래스
     */
    public static class TypingInfo {
        private Long userId;
        private String userName;
        private boolean typing;

        // Constructors
        public TypingInfo() {}

        public TypingInfo(Long userId, String userName, boolean typing) {
            this.userId = userId;
            this.userName = userName;
            this.typing = typing;
        }

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public boolean isTyping() { return typing; }
        public void setTyping(boolean typing) { this.typing = typing; }

        @Override
        public String toString() {
            return String.format("TypingInfo{userId=%d, userName='%s', typing=%s}",
                    userId, userName, typing);
        }
    }
}