package com.meloncity.citiz.domain;

import com.meloncity.citiz.domain.common.BaseTimeEntity;
import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.domain.PostTag;
import com.meloncity.citiz.domain.Tag;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post", indexes = {
        @Index(name = "ix_post_created_by", columnList = "created_by"),
        @Index(name = "ix_post_create_date", columnList = "create_date")
})
@SequenceGenerator(name = "post_seq", sequenceName = "post_seq", allocationSize = 1)
@Getter
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_seq")
    private Long id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private Profile createdBy;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostPhoto> photos = new ArrayList<>();

    // 조인 엔티티 방식 (post_tag)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    public void addPhoto(PostPhoto photo) {
        photos.add(photo);
        photo.setPost(this);
    }

    public void addTag(Tag tag) {
        PostTag pt = new PostTag(this, tag);
        postTags.add(pt);
        tag.getPostTags().add(pt);
    }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setCreatedBy(Profile createdBy) { this.createdBy = createdBy; }
}
