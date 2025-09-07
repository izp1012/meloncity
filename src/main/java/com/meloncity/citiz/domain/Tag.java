package com.meloncity.citiz.domain;

import com.meloncity.citiz.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tag", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tag_tag", columnNames = "tag")
})
@SequenceGenerator(name = "tag_seq", sequenceName = "tag_seq", allocationSize = 1)
public class Tag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_seq")
    private Long id;

    @Column(name = "tag", nullable = false, length = 100)
    private String tag;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    // getters/setters
    public Long getId() { return id; }
    public String getTag() { return tag; }
    public List<PostTag> getPostTags() { return postTags; }

    public void setTag(String tag) { this.tag = tag; }
}
