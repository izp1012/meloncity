package com.meloncity.citiz.service;

import com.meloncity.citiz.domain.Comment;
import com.meloncity.citiz.domain.Post;
import com.meloncity.citiz.dto.CommentReqDto;
import com.meloncity.citiz.repository.CommentRepository;
import com.meloncity.citiz.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public void saveComment(CommentReqDto commentReqDto){
        Optional<Post> optional = postRepository.findById(commentReqDto.getPostId());
        Post post = optional.get();

        Comment comment = new Comment(commentReqDto.getContent(), post, null);

        commentRepository.save(comment);
    }

    public void updateComment(CommentReqDto commentReqDto){
        Optional<Comment> optional = commentRepository.findById(commentReqDto.getCommentId());
        Comment comment = optional.get();

        comment.updateContent(commentReqDto.getContent());

        commentRepository.save(comment);
    }

    public void deleteComment(Long id){
        commentRepository.deleteById(id);
    }

    public void getComments(Long id){

    }
}
