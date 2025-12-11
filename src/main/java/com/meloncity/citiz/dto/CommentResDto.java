package com.meloncity.citiz.dto;

import com.meloncity.citiz.domain.Comment;
import com.meloncity.citiz.domain.Post;
import com.meloncity.citiz.domain.Profile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentResDto {

    private Long id;
    // 게시글
    private Long postId;
    // 작성자
    private Long profileId;
    private String author; //name
    private String avatar; //img

    private Long commentId;
    private String content;

    private Long subCommentId;

    public CommentResDto(Profile profile, Comment comment){
        this.id = comment.getId();
        this.profileId = profile.getId();
        this.author = profile.getName();
        this.avatar = profile.getImageUrl();
        this.content = comment.getContent();
    }
}
