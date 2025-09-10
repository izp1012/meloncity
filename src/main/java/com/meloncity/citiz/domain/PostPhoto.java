package com.meloncity.citiz.domain;

import com.meloncity.citiz.domain.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "post_photo", indexes = {
        @Index(name = "ix_post_photo_post_id", columnList = "post_id")
})
@SequenceGenerator(name = "post_photo_seq", sequenceName = "post_photo_seq", allocationSize = 1)
public class PostPhoto extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_photo_seq")
    private Long id;

    @Column(name = "img_url", nullable = false, length = 1000)
    private String imgUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // getters/setters
    public Long getId() { return id; }
    public String getImgUrl() { return imgUrl; }
    public Post getPost() { return post; }

    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }
    public void setPost(Post post) { this.post = post; }
}
