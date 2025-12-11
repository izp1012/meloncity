package com.meloncity.citiz.service;

import com.meloncity.citiz.domain.Comment;
import com.meloncity.citiz.domain.Post;
import com.meloncity.citiz.domain.Profile;
import com.meloncity.citiz.dto.CommentReqDto;
import com.meloncity.citiz.dto.CommentResDto;
import com.meloncity.citiz.dto.CustomUserDetails;
import com.meloncity.citiz.dto.ResponseDto;
import com.meloncity.citiz.handler.exception.ResourceNotFoundException;
import com.meloncity.citiz.repository.CommentRepository;
import com.meloncity.citiz.repository.PostRepository;
import com.meloncity.citiz.repository.ProfileRepository;
import com.meloncity.citiz.util.CustomDateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final ProfileRepository profileRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public CommentResDto saveComment(Long id, CommentReqDto commentReqDto, CustomUserDetails user){
        Profile profile = profileRepository.findById(user.getId()).orElseThrow(() -> new ResourceNotFoundException("Profile", "id", user.getId()));
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", "id", id.toString()));

        Comment comment = new Comment(profile, post, commentReqDto.getContent(), null);

        commentRepository.save(comment);

        return new CommentResDto(profile, comment);
    }

    public ResponseDto updateComment(Long id, CommentReqDto commentReqDto, CustomUserDetails user){
        String result;
        int resultCode;

        Comment comment = commentRepository.findById(commentReqDto.getCommentId()).orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentReqDto.getCommentId()));
        if(comment.getPost().getId() != id){
            resultCode = -1;
            result = "PERMISSION DENIED";
        }else if(comment.getCreatedBy().getId() != user.getId()){
            resultCode = -1;
            result = "PERMISSION DENIED";
        }else{
            comment.updateContent(commentReqDto.getContent());
            resultCode = 1;
            result = commentReqDto.getContent();
        }
        return new ResponseDto<>(
                    resultCode,
                    result,
                    result,
                    CustomDateUtil.toStringFormat(LocalDateTime.now())
                );
    }

    public String deleteComment(Long id, CommentReqDto commentReqDto, CustomUserDetails user){
        String result = "SUCCESS";

        Comment comment = commentRepository.findById(commentReqDto.getCommentId()).orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentReqDto.getCommentId()));

        if(comment.getPost().getId() != id){
            result = "PERMISSION DENIED";
        }else if(comment.getCreatedBy().getId() != user.getId()){
            result = "PERMISSION DENIED";
        }else{
            commentRepository.deleteById(commentReqDto.getCommentId());
        }

        return result;
    }
}
