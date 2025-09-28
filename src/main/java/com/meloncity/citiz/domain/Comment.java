package com.meloncity.citiz.domain;

import com.meloncity.citiz.domain.common.BaseTimeEntity;
import com.meloncity.citiz.domain.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment", indexes = {
        @Index(name = "ix_comment_post_id", columnList = "post_id"),
        @Index(name = "ix_comment_parent_id", columnList = "sub_comment_id")
})
@SequenceGenerator(name = "comment_seq", sequenceName = "comment_seq", allocationSize = 1)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_seq")
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /**
     * 스키마의 sub_comment_id (NVARCHAR2) 를 '부모 댓글 ID'로 해석해 자기참조 매핑.
     * 실제 DB 타입은 숫자가 자연스럽습니다(가능하면 BIGINT로 변경 권장).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_comment_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();


    //--------------생성자---------------------------

    public Comment(String content, Post post, Comment parent){
        this.content = content;
        this.post = post;
        this.parent = parent;
    }

    public void updateContent(String content){
        this.content = content;
    }
}
