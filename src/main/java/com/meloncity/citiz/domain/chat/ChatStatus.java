package com.meloncity.citiz.domain.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChatStatus {
    SENT("서버로 전달됨/수신자 받지못함"), DELIVERED("수신자 읽었는지 여부 파악X"), READ("수신자 읽은상태");

    private String value;
}