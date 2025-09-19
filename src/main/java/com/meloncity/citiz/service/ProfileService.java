package com.meloncity.citiz.service;

import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.dto.ProfileSignUpReq;
import com.meloncity.citiz.handler.exception.CustomApiException;
import com.meloncity.citiz.handler.exception.ResourceNotFoundException;
import com.meloncity.citiz.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
            throw new CustomApiException(HttpStatus.CONFLICT, "email already exists");
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

    public Profile findById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", id));
    }

    public AuthResult login(String email, String password) {
        Profile user = profileRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "email", email));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomApiException(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다.");
        }

        return new AuthResult(user.getId(), user.getName(), user.getEmail());
    }

    public record AuthResult(Long id, String name, String email) {}
}
