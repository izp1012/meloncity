package com.meloncity.citiz.controller;

import com.meloncity.citiz.dto.*;
import com.meloncity.citiz.service.ProfileService;
import com.meloncity.citiz.util.CustomDateUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<ResponseDto<ProfileSignUpResp>> signUp(@Valid @RequestBody ProfileSignUpReq req) {
        String strName = profileService.signUp(req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto<>(
                  1,
                        new ProfileSignUpResp(strName),
                        "회원가입 완료",
                        CustomDateUtil.toStringFormat(LocalDateTime.now())
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<LoginRes>> login(@RequestBody LoginReq req) {
        ProfileService.AuthResult result = profileService.login(req.email(), req.password());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto<>(
                        1,
                        new LoginRes(result.name()),
                        "로그인 성공",
                        CustomDateUtil.toStringFormat(LocalDateTime.now())
                ));
    }

}
