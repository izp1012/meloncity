package com.meloncity.citiz.repository;


import com.meloncity.citiz.domain.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = {"postTags", "postTags.tag"})
    List<Post> findAll();
}
