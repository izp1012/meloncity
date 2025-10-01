package com.meloncity.citiz.dto;

import com.meloncity.citiz.domain.Profile;

public record ProfileRespDto(
        Long id,
        String name,
        String email,
        String imageUrl
) {
    public static ProfileRespDto from(Profile profile) {
        return new ProfileRespDto(
                profile.getId(),
                profile.getName(),
                profile.getEmail(),
                profile.getImageUrl());
    }

}
