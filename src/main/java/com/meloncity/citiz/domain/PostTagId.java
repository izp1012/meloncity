package com.meloncity.citiz.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
public class PostTagId implements Serializable {
    private Long postId;
    private Long tagId;

    public PostTagId() {}
    public PostTagId(Long postId, Long tagId) {
        this.postId = postId;
        this.tagId = tagId;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostTagId that)) return false;
        return Objects.equals(postId, that.postId) && Objects.equals(tagId, that.tagId);
    }

    @Override public int hashCode() {
        return Objects.hash(postId, tagId);
    }
}
