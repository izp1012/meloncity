package com.meloncity.citiz.service;

import com.meloncity.citiz.domain.chat.ChatRoom;
import com.meloncity.citiz.dto.ChatRoomReqDto;
import com.meloncity.citiz.dto.ChatRoomRespDto;
import com.meloncity.citiz.handler.exception.ResourceNotFoundException;
import com.meloncity.citiz.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public void save(ChatRoom chatRoom) {
        chatRoomRepository.save(chatRoom);
    }

    public ChatRoom findById(Long id) {
        return chatRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", id));
    }

    @Transactional
    public ChatRoomRespDto createChatRoom(ChatRoomReqDto chatRoomReqDto) {
        ChatRoom chatRoom = ChatRoom.builder().build(); // ChatRoomReqDto에 필드가 없어 빌더만 사용
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        return ChatRoomRespDto.from(savedChatRoom);
    }

    public List<ChatRoomRespDto> findAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        return chatRooms.stream()
                .map(ChatRoomRespDto::from)
                .collect(Collectors.toList());
    }
}
