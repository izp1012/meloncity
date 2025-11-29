package com.meloncity.citiz.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class PostReqDto {

    private Long profileId;

    // post Entity
    private Long postId;
    private String title;
    private String content;

    //post_photo Entity
    private List<String> imgUrls; // 파일 경로
    private List<MultipartFile> images; // 실제 파일 데이터

    //post_tag Entity
    private List<String> tagIds;
}
