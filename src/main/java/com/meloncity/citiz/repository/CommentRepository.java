package com.meloncity.citiz.repository;


import com.meloncity.citiz.domain.Comment;
import com.meloncity.citiz.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
