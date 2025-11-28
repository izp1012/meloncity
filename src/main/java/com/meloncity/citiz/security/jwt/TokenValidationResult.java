package com.meloncity.citiz.security.jwt;

public enum TokenValidationResult {
    SUCCESS, EXPIRED, INVALID_SIGNATURE, MALFORMED, UNSUPPORTED, AUDIENCE_INVALID, EMPTY_OR_NULL, INVALID_TOKEN
}