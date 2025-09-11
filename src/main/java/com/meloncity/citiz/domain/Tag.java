package com.meloncity.citiz.domain;

import com.meloncity.citiz.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tag", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tag_tag", columnNames = "tag")
})
@SequenceGenerator(name = "tag_seq", sequenceName = "tag_seq", allocationSize = 1)
@Getter
public class Tag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_seq")
    private Long id;

    @Column(name = "tag", nullable = false, length = 100)
    private String tag;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    public void setTag(String tag) { this.tag = tag; }
}
