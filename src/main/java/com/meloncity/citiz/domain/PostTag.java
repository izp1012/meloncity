package com.meloncity.citiz.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_tag", indexes = {
        @Index(name = "ix_post_tag_post_id", columnList = "post_id"),
        @Index(name = "ix_post_tag_tag_id", columnList = "tag_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTag {

    @EmbeddedId
    private PostTagId id = new PostTagId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;


    //------------생성자-----------------------

    public PostTag(Post post, Tag tag){
        this.post = post;
        this.tag = tag;
    }
}
