package com.meloncity.citiz.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class ProfileSignUpReq {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 2, max = 20)
    private String name;

    @NotBlank
//    @Size(min = 8, max = 100)
    private String password;

    // ✅ 실제 파일을 받을 필드
    private MultipartFile profileImage;
}