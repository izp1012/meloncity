package com.meloncity.citiz.controller;

import com.meloncity.citiz.dto.ProfileSignUpReq;
import com.meloncity.citiz.dto.ProfileSignUpResp;
import com.meloncity.citiz.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<ProfileSignUpResp> signUp(@Valid @RequestBody ProfileSignUpReq req) {
        String strName = profileService.signUp(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProfileSignUpResp(strName));
    }

}
