package com.meloncity.citiz.service;

import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.dto.PageRes;
import com.meloncity.citiz.dto.ProfileRes;
import com.meloncity.citiz.dto.ProfileSignUpReq;
import com.meloncity.citiz.handler.exception.CustomApiException;
import com.meloncity.citiz.handler.exception.ResourceNotFoundException;
import com.meloncity.citiz.repository.ProfileRepository;
import com.meloncity.citiz.util.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Transactional
    public String signUp(ProfileSignUpReq req) {

        if (profileRepository.existsByEmail(req.getEmail())) {
            throw new CustomApiException(HttpStatus.CONFLICT, "email already exists");
        }

        String fileDir = null;
        MultipartFile file = req.getProfileImage();
        if (file != null && !file.isEmpty()) {
            try {
                fileDir = fileStorageService.upload(file);
            } catch (IOException e) {
                throw new CustomApiException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 실패");
            }
        }

        Profile profile = Profile.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .imageUrl(fileDir)
                .build();

        profileRepository.save(profile);

        return profile.getName();
    }

    public Profile findById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", id));
    }

    public Profile findByEmail(String email) {
        return profileRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "email", email));
    }

    public AuthResult login(String email, String password) {
        Profile user = profileRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "email", email));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomApiException(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다.");
        }

        //권한 수정 필요함
        return new AuthResult(user.getId(), user.getName(), user.getEmail(), user.getImageUrl(), java.util.List.of("ROLE_USER"));
    }

    public PageRes<ProfileRes> searchProfile(String email, String name, int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Profile> result = null;
        if (name != null && !name.isBlank() && email != null && !email.isBlank()) {
            result = profileRepository.findByNameContainingIgnoreCaseAndEmailContainingIgnoreCase(name, email, pageable);
        } else if (name != null && !name.isBlank()) {
            result = profileRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (email != null && !email.isBlank()) {
            result = profileRepository.findByEmailContainingIgnoreCase(email, pageable);
        }

        List<ProfileRes> content = result.map(this::toRes).getContent();
        return new PageRes<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.hasNext());
    }

    private ProfileRes toRes(Profile p) {
        return new ProfileRes(
                p.getId(), p.getEmail(), p.getName(), p.getImageUrl()
        );
    }

    public record AuthResult(Long id, String name, String email, String imgUrl, List<String> roles) {}
}
