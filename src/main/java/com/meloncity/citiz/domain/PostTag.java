package com.meloncity.citiz.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "post_tag", indexes = {
        @Index(name = "ix_post_tag_post_id", columnList = "post_id"),
        @Index(name = "ix_post_tag_tag_id", columnList = "tag_id")
})
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

    public PostTag() {}
    public PostTag(Post post, Tag tag) {
        this.post = post;
        this.tag = tag;
        this.id = new PostTagId(post.getId(), tag.getId());
    }

    public PostTagId getId() { return id; }
    public Post getPost() { return post; }
    public Tag getTag() { return tag; }

    public void setPost(Post post) { this.post = post; }
    public void setTag(Tag tag) { this.tag = tag; }
}
