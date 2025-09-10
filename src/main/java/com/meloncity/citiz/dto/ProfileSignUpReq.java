package com.meloncity.citiz.dto;

import jakarta.validation.constraints.*;

public record ProfileSignUpReq (
    @NotBlank @Email String email,
    @NotBlank @Size(min=2, max = 20) String name,
    @NotBlank @Size(min=8, max = 100) String password,
    @Size(max = 500) String imageUrl
) {}
