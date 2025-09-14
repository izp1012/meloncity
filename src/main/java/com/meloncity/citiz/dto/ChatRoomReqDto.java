package com.meloncity.citiz.dto;


import jakarta.validation.constraints.NotNull;

public record ChatRoomReqDto(
        @NotNull Long userId
) {
}