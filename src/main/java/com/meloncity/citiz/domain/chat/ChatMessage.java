package com.meloncity.citiz.domain.chat;

import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 엔티티
 * Redis Stream에서 처리된 메시지를 데이터베이스에 영구 저장하기 위한 엔티티
 */
@Entity
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_message_seq")
    @SequenceGenerator(name = "chat_message_seq", sequenceName = "chat_message_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoom room;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Profile sender;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ChatStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private MessageType type;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreatedDate
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    /**
     * Redis Stream ID (메시지 추적을 위한 필드)
     */
    @Column(name = "stream_id")
    private String streamId;

    /**
     * 메시지를 읽음 처리하는 메서드
     */
    public void markAsRead() {
        this.status = ChatStatus.READ;
        this.readAt = LocalDateTime.now();
    }

    /**
     * 메시지를 전달됨으로 처리하는 메서드
     */
    public void markAsDelivered() {
        this.status = ChatStatus.DELIVERED;
    }

}