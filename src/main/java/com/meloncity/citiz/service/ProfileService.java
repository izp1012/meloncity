package com.meloncity.citiz.service;

import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.dto.ProfileSignUpReq;
import com.meloncity.citiz.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String signUp (ProfileSignUpReq req) {

        if (profileRepository.existsByEmail(req.email())) {
            throw new DuplicateKeyException("email already exists");
        }
        Profile profile = Profile.builder()
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .name(req.name())
                .imageUrl(req.imageUrl())
                .build();
        profileRepository.save(profile);

        return profile.getName();
    }



}
