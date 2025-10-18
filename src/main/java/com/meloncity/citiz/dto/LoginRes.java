package com.meloncity.citiz.dto;

public record LoginRes(String name, String token, long expiresInSeconds) {
}
