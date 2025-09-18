package com.meloncity.citiz.service;

import com.meloncity.citiz.domain.chat.ChatMessage;
import com.meloncity.citiz.domain.chat.ChatRoom;
import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.domain.chat.MessageType;
import com.meloncity.citiz.dto.ChatMessageReqDto;
import com.meloncity.citiz.dto.ChatMessageRespDto;
import com.meloncity.citiz.repository.ChatMessageRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final ProfileService profileService;

    public ChatMessageRespDto sendMessage(@Valid ChatMessageReqDto chatReqDto, Long roomId, MessageType messageType) {
        Profile profile = profileService.findById(chatReqDto.senderId());
        ChatRoom chatRoom = chatRoomService.findById(roomId);

        ChatMessage chatMessage = ChatMessageReqDto.toEntity(profile, chatReqDto.content(), chatRoom, messageType);
        ChatMessage chatPersistence = chatMessageRepository.save(chatMessage);

        return ChatMessageRespDto.from(chatPersistence);
    }
}
