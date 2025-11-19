package com.meloncity.citiz.dto;

import com.meloncity.citiz.security.jwt.RedisJwtStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RedisJwtDto {
    private final String refreshToken;
    private final RedisJwtStatus status;
}
