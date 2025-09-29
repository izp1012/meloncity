package com.meloncity.citiz.controller;

import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.dto.*;
import com.meloncity.citiz.service.ChatService;
import com.meloncity.citiz.service.ProfileService;
import com.meloncity.citiz.service.PubSubService;
import com.meloncity.citiz.util.CustomDateUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅 관련 REST API 및 WebSocket 메시지 처리를 담당하는 컨트롤러
 * Redis Pub/Sub과 Stream을 활용한 실시간 채팅 시스템
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final PubSubService pubSubService;
    private final ProfileService profileService;

    /**
     * 새로운 채팅방을 생성합니다. (REST API)
     * @param request 채팅방 생성 요청 DTO
     * @param creatorId 생성자 ID
     * @return 생성된 채팅방 정보
     */
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createChatRoom(
            @Valid @RequestBody ChatRoomCreateRequest request,
            @RequestHeader("User-Id") Long creatorId) {

        log.info("채팅방 생성 요청 - Creator: {}, Name: {}", creatorId, request.getName());

        try {
            ChatRoomResponse response = chatService.createChatRoom(request, creatorId);

            log.info("채팅방 생성 완료 - Room ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("채팅방 생성 실패: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * [관리자/개발자용] 모든 채팅방 목록을 조회합니다. (ChatRoomController의 getAllChatRooms 기능 통합)
     * NOTE: 실제 사용자 서비스에서는 getUserChatRooms를 사용해야 합니다.
     * @return 모든 채팅방 목록
     */
    @GetMapping("/rooms/all")
    public ResponseEntity<List<ChatRoomResponse>> getAllChatRooms() {
        log.info("모든 채팅방 목록 조회 요청 (Admin/Dev)");
        try {
            List<ChatRoomResponse> chatRooms = chatService.findAllChatRooms();
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            log.error("모든 채팅방 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 채팅방에 참여합니다. (REST API)
     * @param roomId 참여할 채팅방 ID
     * @param userId 사용자 ID
     * @return 성공 여부
     */
    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<ResponseDto<String>> joinChatRoom(
            @PathVariable Long roomId,
            @RequestHeader("User-Id") Long userId) {

        log.info("채팅방 참여 요청 - Room: {}, User: {}", roomId, userId);

        try {
            boolean success = chatService.joinChatRoom(roomId, userId);

            if (success) {
                // 사용자 정보 조회 및 입장 알림 발행
                Profile profile = profileService.findById(userId);
                ProfileRespDto profileRespDto = ProfileRespDto.from(profile);

                pubSubService.publishJoinNotification(roomId, userId, profileRespDto.name());

                log.info("채팅방 참여 완료 - Room: {}, User: {}", roomId, userId);
                return ResponseEntity.ok(new ResponseDto<>(1, "SUCCESS",
                        "채팅방 참여가 완료되었습니다", CustomDateUtil.toStringFormat(LocalDateTime.now())));
            } else {
                return ResponseEntity.badRequest().body(new ResponseDto<>(-1, "FAILED",
                        "채팅방 참여가 실패했습니다", CustomDateUtil.toStringFormat(LocalDateTime.now())));
            }
        } catch (Exception e) {
            log.error("채팅방 참여 중 오류 발생 - Room: {}, User: {}", roomId, userId, e);
            return ResponseEntity.internalServerError().body(new ResponseDto<>(-1, "ERROR",
                    "서버 오류가 발생했습니다", CustomDateUtil.toStringFormat(LocalDateTime.now())));
        }
    }

    /**
     * 채팅방에서 퇴장합니다.
     * @param roomId 퇴장할 채팅방 ID
     * @param userId 퇴장할 사용자 ID
     * @return 성공 여부
     */
    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<ResponseDto<String>> leaveChatRoom(
            @PathVariable Long roomId,
            @RequestHeader("User-Id") Long userId) {

        log.info("채팅방 퇴장 요청 - Room: {}, User: {}", roomId, userId);

        try {
            boolean success = chatService.leaveChatRoom(roomId, userId);

            if (success) {
                // 사용자 정보 조회 및 퇴장 알림 발행
                Profile profile = profileService.findById(userId);
                ProfileRespDto profileRespDto = ProfileRespDto.from(profile);

                pubSubService.publishLeaveNotification(roomId, userId, profileRespDto.name());

                log.info("채팅방 퇴장 완료 - Room: {}, User: {}", roomId, userId);
                return ResponseEntity.ok(new ResponseDto<>(1, "SUCCESS",
                        "채팅방 퇴장이 완료되었습니다", CustomDateUtil.toStringFormat(LocalDateTime.now())));
            } else {
                return ResponseEntity.badRequest().body(new ResponseDto<>(-1, "FAILED",
                        "채팅방 퇴장에 실패했습니다", CustomDateUtil.toStringFormat(LocalDateTime.now())));
            }
        } catch (Exception e) {
            log.error("채팅방 퇴장 중 오류 발생 - Room: {}, User: {}", roomId, userId, e);
            return ResponseEntity.internalServerError().body(new ResponseDto<>(-1, "ERROR",
                    "서버 오류가 발생했습니다", CustomDateUtil.toStringFormat(LocalDateTime.now())));
        }
    }

    /**
     * 사용자가 참여한 채팅방 목록을 조회합니다.
     * @param userId 조회할 사용자 ID
     * @return 참여 중인 채팅방 목록
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getUserChatRooms(
            @RequestHeader("User-Id") Long userId) {

        log.info("사용자 채팅방 목록 조회 - User: {}", userId);

        try {
            List<ChatRoomResponse> chatRooms = chatService.getUserChatRooms(userId);
            log.info("채팅방 목록 조회 완료 - User: {}, Count: {}", userId, chatRooms.size());
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            log.error("채팅방 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 채팅방 정보를 단건 조회합니다. (비효율적인 로직을 chatService 위임으로 수정)
     * @param roomId 조회할 채팅방 ID
     * @param userId 요청한 사용자 ID
     * @return 채팅방 정보
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomResponse> getChatRoom(
            @PathVariable Long roomId,
            @RequestHeader("User-Id") Long userId) {

        log.info("채팅방 정보 조회 - Room: {}, User: {}", roomId, userId);

        try {
            // 💡 단건 조회 메서드를 사용하도록 수정 (ChatService에 getChatRoom이 추가되었다고 가정)
            ChatRoomResponse chatRoom = chatService.getChatRoom(roomId, userId);

            log.info("채팅방 정보 조회 완료 - Room: {}", roomId);
            return ResponseEntity.ok(chatRoom);

        } catch (IllegalArgumentException e) {
            log.warn("채팅방 정보 조회 실패 - Room: {}, User: {}, Reason: {}",
                    roomId, userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("채팅방 정보 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 채팅방의 메시지 히스토리를 조회합니다.
     * @param roomId 채팅방 ID
     * @param userId 요청한 사용자 ID
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 50, 최대: 100)
     * @return 메시지 히스토리 목록
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageHistoryResponse>> getChatHistory(
            @PathVariable Long roomId,
            @RequestHeader("User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.info("채팅 히스토리 조회 - Room: {}, User: {}, Page: {}, Size: {}",
                roomId, userId, page, size);

        try {
            // 페이지 크기 제한 (최대 100개)
            size = Math.min(size, 100);

            List<ChatMessageHistoryResponse> messages = chatService.getChatHistory(roomId, userId, page, size);
            log.info("채팅 히스토리 조회 완료 - Room: {}, Messages: {}", roomId, messages.size());
            return ResponseEntity.ok(messages);

        } catch (IllegalArgumentException e) {
            log.warn("채팅 히스토리 조회 실패 - Room: {}, User: {}, Reason: {}",
                    roomId, userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("채팅 히스토리 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 메시지를 읽음 처리합니다.
     * @param messageId 읽음 처리할 메시지 ID
     * @param userId 메시지를 읽은 사용자 ID
     * @return 읽음 처리 결과
     */
    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<ResponseDto<String>> markMessageAsRead(
            @PathVariable Long messageId,
            @RequestHeader("User-Id") Long userId) {

        log.info("메시지 읽음 처리 요청 - Message: {}, User: {}", messageId, userId);

        try {
            chatService.markMessageAsRead(messageId, userId);
            log.info("메시지 읽음 처리 완료 - Message: {}, User: {}", messageId, userId);

            return ResponseEntity.ok(new ResponseDto<>(1, "SUCCESS",
                    "메시지가 읽음 처리되었습니다", CustomDateUtil.toStringFormat(LocalDateTime.now())));

        } catch (IllegalArgumentException e) {
            log.warn("메시지 읽음 처리 실패 - Message: {}, User: {}, Reason: {}",
                    messageId, userId, e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseDto<>(-1, "FAILED",
                    e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now())));
        } catch (Exception e) {
            log.error("메시지 읽음 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(new ResponseDto<>(-1, "ERROR",
                    "서버 오류가 발생했습니다", CustomDateUtil.toStringFormat(LocalDateTime.now())));
        }
    }

    /**
     * STOMP를 통해 채팅 메시지를 수신하고 Redis Stream에 발행합니다. (WebSocket)
     * 클라이언트가 '/app/chat/message'로 메시지를 보낼 때 호출됩니다.
     * @param messageDto 수신된 메시지 DTO
     */
    @MessageMapping("/chat/message")
    public void receiveAndPublishMessage(@Payload ChatMessageDto messageDto) {
        log.info("WebSocket을 통해 메시지 수신 : {}", messageDto.getContent());

        try {
            // 메시지 검증
            validateMessage(messageDto);

            String streamId = chatService.publishMessage(messageDto);
            log.info("메시지 Stream 발행 완료 - Stream ID: {}, Room: {}", streamId, messageDto.getRoomId());

        } catch (IllegalArgumentException e) {
            log.error("메시지 발행 실패: {}", e.getMessage());
            // TODO: 클라이언트에게 오류 메시지 전송
        } catch (Exception e) {
            log.error("메시지 처리 중 예상치 못한 오류 발생", e);
        }
    }

    /**
     * 메시지 유효성을 검증합니다.
     * @param messageDto 검증할 메시지 DTO
     * @throws IllegalArgumentException 유효하지 않은 메시지인 경우
     */
    private void validateMessage(ChatMessageDto messageDto) {
        if (messageDto.getRoomId() == null || messageDto.getRoomId() <= 0) {
            throw new IllegalArgumentException("유효하지 않은 채팅방 ID입니다");
        }

        if (messageDto.getSenderId() == null || messageDto.getSenderId() <= 0) {
            throw new IllegalArgumentException("유효하지 않은 발신자 ID입니다");
        }

        if (messageDto.getContent() == null || messageDto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용이 비어있습니다");
        }

        if (messageDto.getContent().length() > 1000) {
            throw new IllegalArgumentException("메시지가 너무 깁니다 (최대 1000자)");
        }
    }
}