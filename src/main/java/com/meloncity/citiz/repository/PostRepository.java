package com.meloncity.citiz.repository;


import com.meloncity.citiz.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
