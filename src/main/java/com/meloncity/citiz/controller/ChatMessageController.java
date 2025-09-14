package com.meloncity.citiz.controller;

import com.meloncity.citiz.domain.chat.MessageType;
import com.meloncity.citiz.dto.ChatMessageReqDto;
import com.meloncity.citiz.dto.ChatMessageRespDto;
import com.meloncity.citiz.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/chat")
@Slf4j
public class ChatMessageController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage/{roomId}")
    public void sendMessage(@Payload @Valid ChatMessageReqDto chatMessageReqDto, @DestinationVariable Long roomId) {
        ChatMessageRespDto chatRespDto = chatService.sendMessage(chatMessageReqDto, roomId, MessageType.USER);

        messagingTemplate.convertAndSend("/topic/public/" + roomId, chatRespDto);
    }
}
