package com.meloncity.citiz.dto;

import lombok.Getter;
import lombok.Setter;

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
    private List<String> imgUrls;

    //post_tag Entity
    private List<String> tagIds;
}
