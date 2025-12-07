package com.meloncity.citiz.dto;

public record LoginRes(
        Long id
        ,String name
        ,String email
        , String imgUrl
        , String token
        , long expiresInSeconds) {
}
