package com.meloncity.citiz.domain;

import com.meloncity.citiz.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_photo", indexes = {
        @Index(name = "ix_post_photo_post_id", columnList = "post_id")
})
@SequenceGenerator(name = "post_photo_seq", sequenceName = "post_photo_seq", allocationSize = 1)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostPhoto extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_photo_seq")
    private Long id;

    @Column(name = "img_url", nullable = false, length = 1000)
    private String imgUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;


    public PostPhoto(String imgUrl, Post post){
        this.imgUrl = imgUrl;
        this.post = post;
    }
}
