package com.meloncity.citiz.repository;

import com.meloncity.citiz.domain.Post;
import com.meloncity.citiz.domain.PostTag;
import com.meloncity.citiz.domain.PostTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {
    void deleteAllByPost(Post post);
}
