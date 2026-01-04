package com.meloncity.citiz.controller;

import com.meloncity.citiz.dto.*;
import com.meloncity.citiz.security.jwt.JwtTokenProvider;
import com.meloncity.citiz.service.ProfileService;
import com.meloncity.citiz.util.CustomDateUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final ProfileService profileService;
    private final JwtTokenProvider jwtTokenProvider;

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
    public ResponseEntity<ResponseDto<LoginRes>> login(@RequestBody LoginReq req, HttpServletResponse response) {

        ProfileService.AuthResult result = profileService.login(req.email(), req.password());

        var roles = (result.roles() != null && !result.roles().isEmpty())
                ? result.roles()
                : java.util.List.of("ROLE_USER");

        String token = jwtTokenProvider.createToken(result.email(), roles);
        jwtTokenProvider.createRefreshToken(result.email(), roles, response);

        LoginRes payload = new LoginRes(
                result.id(),
                result.name(),
                result.email(),
                result.imgUrl(),
                token,
                jwtTokenProvider.getExpirationSeconds()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto<>(
                        1,
                        payload,
                        "로그인 성공",
                        CustomDateUtil.toStringFormat(LocalDateTime.now())
                ));
    }

    @GetMapping
    public PageRes<ProfileRes> search (
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createDate,desc") String sort
    ) {
        String[] s = sort.split(",");
        Sort.Direction dir = (s.length > 1 && s[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort by = Sort.by(dir, s[0]);
        return profileService.searchProfile(email, name, page, size, by);
    }

}
