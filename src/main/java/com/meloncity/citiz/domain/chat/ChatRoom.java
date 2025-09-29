package com.meloncity.citiz.domain.chat;

import com.meloncity.citiz.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 채팅방 엔티티
 * 채팅방 정보와 참여자 관리를 담당
 */
@Entity
@Table(name = "chat_rooms")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_room_seq")
    @SequenceGenerator(name = "chat_room_seq", sequenceName = "chat_room_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "last_message", columnDefinition = "TEXT")
    private String lastMessage;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatRoomParticipant> participants = new ArrayList<>();

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "is_private")
    @Builder.Default
    private Boolean isPrivate = false;

    /**
     * 마지막 메시지 정보를 업데이트하는 메서드
     * @param message 마지막 메시지 내용
     * @param messageTime 메시지 시간
     */
    public void updateLastMessage(String message, LocalDateTime messageTime) {
        this.lastMessage = message;
        this.lastMessageTime = messageTime;
    }

    /**
     * 채팅방 정보를 업데이트하는 메서드
     * @param name 채팅방 이름
     * @param description 채팅방 설명
     */
    public void updateRoomInfo(String name, String description) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
    }
}