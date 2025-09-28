package com.meloncity.citiz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentReqDto {

    private Long postId;

    private Long commentId;
    private Long subCommentId;
    private String content;
}
