package com.meloncity.citiz.repository;

import com.meloncity.citiz.domain.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 채팅 메시지 엔티티에 대한 데이터 액세스 리포지토리
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 특정 채팅방의 메시지를 생성일시 역순으로 페이징하여 조회
     * @param roomId 채팅방 ID
     * @param offset 시작 오프셋
     * @param limit 조회할 개수
     * @return 메시지 목록
     */
    @Query("SELECT cm FROM ChatMessage cm " +
            "WHERE cm.room.id = :roomId " +
            "ORDER BY cm.createDate DESC " +
            "LIMIT :limit OFFSET :offset")
    List<ChatMessage> findByRoomIdOrderByCreatedAtDesc(
            @Param("roomId") Long roomId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 특정 채팅방의 읽지 않은 메시지 수 조회
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 읽지 않은 메시지 수
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm " +
            "WHERE cm.room.id = :roomId " +
            "AND cm.sender.id != :userId " +
            "AND cm.status != 'READ'")
    Long countUnreadMessages(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * Stream ID로 메시지 조회
     * @param streamId Redis Stream ID
     * @return 메시지 엔티티
     */
    ChatMessage findByStreamId(String streamId);

    /**
     * 특정 사용자가 보낸 메시지 수 조회
     * @param senderId 발신자 ID
     * @return 메시지 수
     */
    Long countBySenderId(Long senderId);
}