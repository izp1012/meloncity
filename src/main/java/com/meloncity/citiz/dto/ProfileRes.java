package com.meloncity.citiz.dto;

public record ProfileRes(
        Long id,
        String email,
        String name,
        String imageUrl
) {
}
