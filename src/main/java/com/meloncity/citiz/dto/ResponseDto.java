package com.meloncity.citiz.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ResponseDto<T> {

    private final Integer code; //1 성공 -1 실패
    private final T data;
    private final String msg;
    private final String localDateTime; //응답 시간
}
