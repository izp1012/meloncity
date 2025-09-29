package com.meloncity.citiz.repository;

import com.meloncity.citiz.domain.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 채팅방 엔티티에 대한 데이터 액세스 리포지토리
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 특정 사용자가 참여한 채팅방 목록 조회
     * @param participantId 참여자 ID
     * @return 참여 중인 채팅방 목록
     */
    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
            "JOIN cr.participants p " +
            "WHERE p.participant.id = :participantId " +
            "AND p.isActive = true " +
            "ORDER BY cr.lastMessageTime DESC")
    List<ChatRoom> findByParticipantId(@Param("participantId") Long participantId);

    /**
     * 공개 채팅방 목록 조회
     * @return 공개 채팅방 목록
     */
    @Query("SELECT cr FROM ChatRoom cr " +
            "WHERE cr.isPrivate = false " +
            "ORDER BY cr.lastMessageTime DESC")
    List<ChatRoom> findPublicChatRooms();

    /**
     * 채팅방 이름으로 검색
     * @param name 채팅방 이름 (부분 일치)
     * @return 검색된 채팅방 목록
     */
    @Query("SELECT cr FROM ChatRoom cr " +
            "WHERE cr.name LIKE %:name% " +
            "AND cr.isPrivate = false " +
            "ORDER BY cr.lastMessageTime DESC")
    List<ChatRoom> findByNameContaining(@Param("name") String name);

    /**
     * 최대 참여자 수가 설정된 채팅방 중 여유가 있는 채팅방 조회
     * @return 여유가 있는 채팅방 목록
     */
    @Query("SELECT cr FROM ChatRoom cr " +
            "WHERE cr.maxParticipants IS NOT NULL " +
            "AND (SELECT COUNT(p) FROM ChatRoomParticipant p WHERE p.room.id = cr.id AND p.isActive = true) < cr.maxParticipants " +
            "AND cr.isPrivate = false " +
            "ORDER BY cr.lastMessageTime DESC")
    List<ChatRoom> findAvailableChatRooms();
}
