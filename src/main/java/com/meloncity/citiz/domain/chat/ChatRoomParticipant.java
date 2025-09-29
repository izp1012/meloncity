package com.meloncity.citiz.domain.chat;

import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 채팅방 참여자 엔티티
 * 채팅방에 참여한 사용자들의 정보를 관리
 */
@Entity
@Table(name = "chat_room_participants", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "participant_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_participant_seq")
    @SequenceGenerator(name = "chat_participant_seq", sequenceName = "chat_participant_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Profile participant;

    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ParticipantRole role = ParticipantRole.MEMBER;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    /**
     * 참여자의 채팅방 퇴장 처리
     */
    public void leave() {
        this.isActive = false;
        this.leftAt = LocalDateTime.now();
    }

    /**
     * 참여자의 채팅방 재입장 처리
     */
    public void rejoin() {
        this.isActive = true;
        this.leftAt = null;
        this.joinedAt = LocalDateTime.now();
    }

    /**
     * 마지막 읽은 메시지 업데이트
     * @param messageId 마지막 읽은 메시지 ID
     */
    public void updateLastReadMessage(Long messageId) {
        this.lastReadMessageId = messageId;
        this.lastReadAt = LocalDateTime.now();
    }

    /**
     * 참여자 역할 변경
     * @param role 새로운 역할
     */
    public void changeRole(ParticipantRole role) {
        this.role = role;
    }

    /**
     * 참여자 역할 열거형
     */
    public enum ParticipantRole {
        ADMIN("관리자"),
        MODERATOR("조정자"), 
        MEMBER("일반 멤버");

        private final String description;

        ParticipantRole(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}