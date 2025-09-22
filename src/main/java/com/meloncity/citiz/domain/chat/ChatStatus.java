package com.meloncity.citiz.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChatStatus {
    SENDING("클라이언트에서 전송 중 (임시 상태)"),
    SENT("서버로 전달됨/수신자 받지못함"),
    DELIVERED("수신자 읽었는지 여부 파악X"),
    READ("수신자 읽은상태"),
    FAILED("전송 실패");

    private final String value;
}