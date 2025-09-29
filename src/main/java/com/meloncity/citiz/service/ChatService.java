package com.meloncity.citiz.service;

import com.meloncity.citiz.config.redis.RedisStreamConfig;
import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.domain.chat.*;
import com.meloncity.citiz.dto.ChatMessageDto;
import com.meloncity.citiz.dto.ChatMessageHistoryResponse;
import com.meloncity.citiz.dto.ChatRoomCreateRequest;
import com.meloncity.citiz.dto.ChatRoomResponse;
import com.meloncity.citiz.handler.exception.CustomApiException;
import com.meloncity.citiz.repository.ChatMessageRepository;
import com.meloncity.citiz.repository.ChatRoomRepository;
import com.meloncity.citiz.repository.ChatRoomParticipantRepository;
import com.meloncity.citiz.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 채팅 관련 비즈니스 로직을 처리하는 서비스
 * Redis Stream을 통한 메시지 처리와 데이터베이스 영속성 관리
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomParticipantRepository participantRepository;
    private final ProfileRepository profileRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private final RedisStreamConfig.StreamSettings streamSettings;

    /**
     * 새로운 채팅방을 생성합니다.
     * @param request 채팅방 생성 요청 정보
     * @param creatorId 채팅방 생성자 ID
     * @return 생성된 채팅방 정보
     */
    public ChatRoomResponse createChatRoom(ChatRoomCreateRequest request, Long creatorId) {
        Profile creator = profileRepository.findById(creatorId)
                .orElseThrow(() -> new CustomApiException(HttpStatus.BAD_REQUEST, "사용자를 찾을 수 없습니다: " + creatorId));

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(request.getName())
                .description(request.getDescription())
                .maxParticipants(request.getMaxParticipants())
                .isPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false)
                .build();

        chatRoom = chatRoomRepository.save(chatRoom);

        // 생성자를 관리자로 참여시키기
        ChatRoomParticipant participant = ChatRoomParticipant.builder()
                .room(chatRoom)
                .participant(creator)
                .role(ChatRoomParticipant.ParticipantRole.ADMIN)
                .build();

        participantRepository.save(participant);

        log.info("새로운 채팅방 생성됨: {} (ID: {})", chatRoom.getName(), chatRoom.getId());

        return mapToChatRoomResponse(chatRoom);
    }

    /**
     * 채팅방에 참여합니다.
     * @param roomId 채팅방 ID
     * @param userId 참여할 사용자 ID
     * @return 성공 여부
     */
    public boolean joinChatRoom(Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomApiException(HttpStatus.BAD_REQUEST, "존재하지 않는 채팅방입니다. : " + roomId));

        Profile user = profileRepository.findById(userId)
                .orElseThrow(() -> new CustomApiException(HttpStatus.BAD_REQUEST, "사용자를 찾을 수 없습니다: " + userId));

        // 최대 참여자 수 확인
        if (chatRoom.getMaxParticipants() != null) {
            long currentParticipants = participantRepository.countByRoomIdAndIsActiveTrue(roomId);
            if (currentParticipants >= chatRoom.getMaxParticipants()) {
                throw new CustomApiException(HttpStatus.BAD_REQUEST, "채팅방이 가득 찼습니다.");
            }
        }

        // 기존 참여자 레코드가 있는지 확인 (재입장 케이스)
        ChatRoomParticipant existingParticipant = participantRepository
                .findByRoomIdAndParticipantId(roomId, userId);

        if (existingParticipant != null) {
            existingParticipant.rejoin();
        } else {
            ChatRoomParticipant newParticipant = ChatRoomParticipant.builder()
                    .room(chatRoom)
                    .participant(user)
                    .build();
            participantRepository.save(newParticipant);
        }

        log.info("사용자 {}가 채팅방 {}에 참여했습니다.", user.getName(), chatRoom.getName());
        return true;
    }

    /**
     * 채팅방에서 퇴장합니다.
     * @param roomId 채팅방 ID
     * @param userId 퇴장할 사용자 ID
     * @return 성공 여부
     */
    public boolean leaveChatRoom(Long roomId, Long userId) {
        ChatRoomParticipant participant = (ChatRoomParticipant) participantRepository
                .findByRoomIdAndParticipantIdAndIsActiveTrue(roomId, userId)
                .orElseThrow(() -> new CustomApiException(HttpStatus.BAD_REQUEST, "채팅방 참여자를 찾을 수 없습니다."));

        participant.leave();
        log.info("사용자 {}가 채팅방 {}에서 퇴장했습니다.", participant.getParticipant().getName(), participant.getRoom().getName());
        return true;
    }

    /**
     * Redis Stream에 채팅 메시지를 발행합니다.
     * @param messageDto 발행할 메시지 정보
     * @return Stream에 추가된 메시지의 ID
     */
    public String publishMessage(ChatMessageDto messageDto) {
        // 채팅방과 발신자 검증
        validateChatRoomAndSender(messageDto.getRoomId(), messageDto.getSenderId());

        // 현재 시간 설정
        if (messageDto.getTimestamp() == null) {
            messageDto.setTimestamp(LocalDateTime.now());
        }
        messageDto.setStatus(ChatStatus.SENT);

        // Redis Stream에 메시지 추가
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("roomId", messageDto.getRoomId());
        messageMap.put("senderId", messageDto.getSenderId());
        messageMap.put("senderName", messageDto.getSenderName());
        messageMap.put("content", messageDto.getContent());
        messageMap.put("type", messageDto.getType().name());
        messageMap.put("status", messageDto.getStatus().name());
        messageMap.put("timestamp", messageDto.getTimestamp().toString());
        messageMap.put("tempId", messageDto.getTempId());

        MapRecord<String, String, Object> record = MapRecord.create(streamSettings.getStreamName(), messageMap);
        String streamId = redisTemplate.opsForStream().add(record).getValue();

        log.info("메시지가 Stream에 추가됨 - Stream ID: {}, Room: {}, Sender: {}",
                streamId, messageDto.getRoomId(), messageDto.getSenderId());

        return streamId;
    }

    /**
     * Redis Stream의 메시지를 데이터베이스에 영구 저장합니다.
     *
     * @param messageDto 저장할 메시지 정보
     * @param streamId   Redis Stream ID
     */
    @Transactional
    public Long saveMessageToDatabase(ChatMessageDto messageDto, String streamId) {
        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getRoomId())
                .orElseThrow(() -> new CustomApiException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다: " + messageDto.getRoomId()));

        Profile sender = profileRepository.findById(messageDto.getSenderId())
                .orElseThrow(() -> new CustomApiException(HttpStatus.NOT_FOUND, "발신자를 찾을 수 없습니다: " + messageDto.getSenderId()));

        ChatMessage chatMessage = ChatMessage.builder()
                .room(chatRoom)
                .sender(sender)
                .content(messageDto.getContent())
                .type(messageDto.getType())
                .status(messageDto.getStatus())
                .streamId(streamId)
                .build();

        chatMessage = chatMessageRepository.save(chatMessage);

        // 채팅방의 마지막 메시지 정보 업데이트
        chatRoom.updateLastMessage(messageDto.getContent(), messageDto.getTimestamp());

        log.info("메시지가 데이터베이스에 저장됨 - Message ID: {}, Stream ID: {}",
                chatMessage.getId(), streamId);

        return chatMessage.getId();
    }

    /**
     * 채팅방의 메시지 히스토리를 조회합니다.
     * @param roomId 채팅방 ID
     * @param userId 요청한 사용자 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 메시지 히스토리 목록
     */
    @Transactional(readOnly = true)
    public List<ChatMessageHistoryResponse> getChatHistory(Long roomId, Long userId, int page, int size) {
        // 사용자가 채팅방 참여자인지 확인
        boolean isParticipant = participantRepository
                .existsByRoomIdAndParticipantIdAndIsActiveTrue(roomId, userId);

        if (!isParticipant) {
            throw new CustomApiException(HttpStatus.NOT_FOUND, "채팅방에 참여하지 않은 사용자입니다.");
        }

        // 페이징을 적용한 메시지 조회
        List<ChatMessage> messages = chatMessageRepository
                .findByRoomIdOrderByCreatedAtDesc(roomId, page * size, size);

        return messages.stream()
                .map(this::mapToChatMessageHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 참여한 채팅방 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 참여 중인 채팅방 목록
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getUserChatRooms(Long userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipantId(userId);

        return chatRooms.stream()
                .map(this::mapToChatRoomResponse)
                .collect(Collectors.toList());
    }

    /**
     * 특정 채팅방을 단건 조회합니다. (Controller 요구사항 반영)
     * @param roomId 채팅방 ID
     * @param userId 요청한 사용자 ID
     * @return 채팅방 정보
     */
    @Transactional(readOnly = true)
    public ChatRoomResponse getChatRoom(Long roomId, Long userId) {
        // 1. 사용자가 해당 채팅방의 참여자인지 확인 (권한 확인)
        boolean isParticipant = participantRepository
                .existsByRoomIdAndParticipantIdAndIsActiveTrue(roomId, userId);

        if (!isParticipant) {
            throw new CustomApiException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없거나 참여하지 않은 사용자입니다.");
        }

        // 2. 채팅방 단건 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomApiException(HttpStatus.NOT_FOUND, "존재하지 않는 채팅방입니다: " + roomId));

        return mapToChatRoomResponse(chatRoom);
    }

    /**
     * 메시지를 읽음 처리합니다.
     * @param messageId 메시지 ID
     * @param userId 읽은 사용자 ID
     */
    public void markMessageAsRead(Long messageId, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new CustomApiException(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다: " + messageId));

        // 사용자가 해당 채팅방 참여자인지 확인
        boolean isParticipant = participantRepository
                .existsByRoomIdAndParticipantIdAndIsActiveTrue(message.getRoom().getId(), userId);

        if (!isParticipant) {
            throw new CustomApiException(HttpStatus.NOT_FOUND, "해당 채팅방의 참여자가 아닙니다.");
        }

        message.markAsRead();

        // 참여자의 마지막 읽은 메시지 업데이트
        ChatRoomParticipant participant = (ChatRoomParticipant) participantRepository
                .findByRoomIdAndParticipantIdAndIsActiveTrue(message.getRoom().getId(), userId)
                .orElseThrow();

        participant.updateLastReadMessage(messageId);

        log.info("메시지 읽음 처리됨 - Message ID: {}, User ID: {}", messageId, userId);
    }

    /**
     * 채팅방과 발신자 유효성을 검증합니다.
     * @param roomId 채팅방 ID
     * @param senderId 발신자 ID
     */
    private void validateChatRoomAndSender(Long roomId, Long senderId) {
        if (!chatRoomRepository.existsById(roomId)) {
            throw new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId);
        }

        if (!profileRepository.existsById(senderId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + senderId);
        }

        // 발신자가 채팅방 참여자인지 확인
        boolean isParticipant = participantRepository
                .existsByRoomIdAndParticipantIdAndIsActiveTrue(roomId, senderId);

        if (!isParticipant) {
            throw new IllegalArgumentException("채팅방에 참여하지 않은 사용자입니다.");
        }
    }

    /**
     * ChatRoom 엔티티를 ChatRoomResponse DTO로 변환합니다.
     */
    private ChatRoomResponse mapToChatRoomResponse(ChatRoom chatRoom) {
        int participantCount = participantRepository.countByRoomIdAndIsActiveTrue(chatRoom.getId()).intValue();

        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .description(chatRoom.getDescription())
                .lastMessage(chatRoom.getLastMessage())
                .lastMessageTime(chatRoom.getLastMessageTime())
                .participantCount(participantCount)
                .maxParticipants(chatRoom.getMaxParticipants())
                .isPrivate(chatRoom.getIsPrivate())
                .timestamp(chatRoom.getCreateDate())
                .build();
    }

    /**
     * ChatMessage 엔티티를 ChatMessageHistoryResponse DTO로 변환합니다.
     */
    private ChatMessageHistoryResponse mapToChatMessageHistoryResponse(ChatMessage message) {
        return ChatMessageHistoryResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getName())
                .content(message.getContent())
                .type(message.getType())
                .status(message.getStatus())
                .timestamp(message.getCreateDate())
                .readAt(message.getReadAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> findAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        return chatRooms.stream()
                .map(this::mapToChatRoomResponse) // 기존 매핑 메서드 활용
                .collect(Collectors.toList());
    }
}