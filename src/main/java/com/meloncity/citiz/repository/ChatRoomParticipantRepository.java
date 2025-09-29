package com.meloncity.citiz.repository;

import com.meloncity.citiz.domain.chat.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 채팅방 참여자 엔티티에 대한 데이터 액세스 리포지토리
 */
@Repository
public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {

    /**
     * 특정 채팅방의 활성 참여자 수 조회
     * @param roomId 채팅방 ID
     * @return 활성 참여자 수
     */
    Long countByRoomIdAndIsActiveTrue(Long roomId);

    /**
     * 특정 사용자가 특정 채팅방에 활성 참여 중인지 확인
     * @param roomId 채팅방 ID
     * @param participantId 참여자 ID
     * @return 참여 여부
     */
    boolean existsByRoomIdAndParticipantIdAndIsActiveTrue(Long roomId, Long participantId);

    /**
     * 특정 사용자의 특정 채팅방 참여 정보 조회 (활성 참여자만)
     * @param roomId 채팅방 ID
     * @param participantId 참여자 ID
     * @return 참여자 정보
     */
    Optional<ChatRoomParticipant> findByRoomIdAndParticipantIdAndIsActiveTrue(Long roomId, Long participantId);

    /**
     * 특정 사용자의 특정 채팅방 참여 정보 조회 (비활성 포함)
     * @param roomId 채팅방 ID
     * @param participantId 참여자 ID
     * @return 참여자 정보
     */
    ChatRoomParticipant findByRoomIdAndParticipantId(Long roomId, Long participantId);

    /**
     * 특정 채팅방의 모든 활성 참여자 목록 조회
     * @param roomId 채팅방 ID
     * @return 활성 참여자 목록
     */
    @Query("SELECT p FROM ChatRoomParticipant p " +
           "WHERE p.room.id = :roomId " +
           "AND p.isActive = true " +
           "ORDER BY p.joinedAt ASC")
    List<ChatRoomParticipant> findActiveParticipantsByRoomId(@Param("roomId") Long roomId);

    /**
     * 특정 사용자가 참여한 모든 활성 채팅방 목록 조회
     * @param participantId 참여자 ID
     * @return 참여 중인 채팅방의 참여자 정보 목록
     */
    @Query("SELECT p FROM ChatRoomParticipant p " +
           "WHERE p.participant.id = :participantId " +
           "AND p.isActive = true " +
           "ORDER BY p.room.lastMessageTime DESC")
    List<ChatRoomParticipant> findActiveRoomsByParticipantId(@Param("participantId") Long participantId);

    /**
     * 특정 채팅방의 관리자 목록 조회
     * @param roomId 채팅방 ID
     * @return 관리자 목록
     */
    @Query("SELECT p FROM ChatRoomParticipant p " +
           "WHERE p.room.id = :roomId " +
           "AND p.isActive = true " +
           "AND p.role = 'ADMIN' " +
           "ORDER BY p.joinedAt ASC")
    List<ChatRoomParticipant> findAdminsByRoomId(@Param("roomId") Long roomId);

    /**
     * 특정 채팅방에서 특정 메시지 ID 이후의 메시지를 읽지 않은 참여자 수 조회
     * @param roomId 채팅방 ID
     * @param messageId 메시지 ID
     * @return 읽지 않은 참여자 수
     */
    @Query("SELECT COUNT(p) FROM ChatRoomParticipant p " +
           "WHERE p.room.id = :roomId " +
           "AND p.isActive = true " +
           "AND (p.lastReadMessageId IS NULL OR p.lastReadMessageId < :messageId)")
    Long countUnreadParticipants(@Param("roomId") Long roomId, @Param("messageId") Long messageId);

    /**
     * 특정 사용자가 읽지 않은 메시지가 있는 채팅방 수 조회
     * @param participantId 참여자 ID
     * @return 읽지 않은 메시지가 있는 채팅방 수
     */
    @Query("SELECT COUNT(DISTINCT p.room.id) FROM ChatRoomParticipant p " +
           "WHERE p.participant.id = :participantId " +
           "AND p.isActive = true " +
           "AND EXISTS (" +
           "  SELECT 1 FROM ChatMessage m " +
           "  WHERE m.room.id = p.room.id " +
           "  AND m.sender.id != :participantId " +
           "  AND (p.lastReadMessageId IS NULL OR m.id > p.lastReadMessageId)" +
           ")")
    Long countRoomsWithUnreadMessages(@Param("participantId") Long participantId);
}