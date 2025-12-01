package com.meloncity.citiz.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Setter
@Getter
public class PostRespDto {
    private Long profileId;
    private String profileName;

    // post Entity
    private Long postId;
    private String title;
    private String content;
    private String createdAt;

    //post_photo Entity
    private List<String> images; // 파일 경로

    //post_tag Entity
    private List<String> tags;
}
