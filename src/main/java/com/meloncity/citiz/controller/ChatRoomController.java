package com.meloncity.citiz.controller;

import com.meloncity.citiz.dto.*;
import com.meloncity.citiz.service.ChatRoomService;
import com.meloncity.citiz.util.CustomDateUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/chat-rooms")
@Slf4j
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody @Valid ChatRoomReqDto chatRoomReqDto) {
        ChatRoomRespDto chatRoomRespDto = chatRoomService.createChatRoom(chatRoomReqDto);
        return new ResponseEntity<>(new ResponseDto<>(1, chatRoomRespDto, "채팅방 생성", CustomDateUtil.toStringFormat(LocalDateTime.now())), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ResponseDto<List<ChatRoomRespDto>>> getAllChatRooms() {
        List<ChatRoomRespDto> chatRooms = chatRoomService.findAllChatRooms();
        return new ResponseEntity<>(new ResponseDto<>(1, chatRooms, "모든 채팅방 가져오기", CustomDateUtil.toStringFormat(LocalDateTime.now())), HttpStatus.OK);
    }
}
