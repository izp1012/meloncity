package com.meloncity.citiz.controller;

import com.meloncity.citiz.dto.*;
import com.meloncity.citiz.service.CommentService;
import com.meloncity.citiz.service.PostService;
import com.meloncity.citiz.util.CustomDateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    // 게시글 전체 조회
    @GetMapping
    public String getAllPosts(){
        postService.getPostAll();
        return "getAllPosts";
    }

    // 게시글 조회
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<PostRespDto>> getPost(@PathVariable Long id){
        PostRespDto postRespDto = postService.getPost(id);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto<>(
                        1,
                        postRespDto,
                        "게시글 조회 완료",
                        CustomDateUtil.toStringFormat(LocalDateTime.now())
                ));
    }

    // 게시글 저장
    @PostMapping
    public ResponseEntity<ResponseDto<String>> savePost(@ModelAttribute PostReqDto postReqDto, @AuthenticationPrincipal CustomUserDetails user) throws IOException {
        postReqDto.setProfileId(user.getId());
        postService.savePost(postReqDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto<>(
                        1,
                        "SUCCESS",
                        "게시글 등록 완료",
                        CustomDateUtil.toStringFormat(LocalDateTime.now())
                ));
    }

    // 게시글 변경
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<String>> updatePost(@PathVariable Long id, @ModelAttribute PostReqDto postReqDto, @AuthenticationPrincipal CustomUserDetails user){
        String result = postService.updatePost(id, postReqDto, user);
        int resultCode = "SUCCESS".equals(result) ? 1 : -1 ;

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto<>(
                        resultCode,
                        result,
                        result,
                        CustomDateUtil.toStringFormat(LocalDateTime.now())
                ));
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<String>> deletePost(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user) throws IOException {
        String result = postService.deletePost(id, user);
        int resultCode = "SUCCESS".equals(result) ? 1 : -1 ;

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto<>(
                        resultCode,
                        result,
                        result,
                        CustomDateUtil.toStringFormat(LocalDateTime.now())
                ));
    }

    // 게시글 댓글 등록
    @PostMapping("/{id}/comments")
    public ResponseEntity saveComment(@PathVariable Long id, @RequestBody CommentReqDto commentReqDto, @AuthenticationPrincipal CustomUserDetails user){
        CommentResDto commentResDto = commentService.saveComment(id, commentReqDto, user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto<>(
                        1,
                        commentResDto,
                        "SUCCESS",
                        CustomDateUtil.toStringFormat(LocalDateTime.now())
                ));
    }

    @PutMapping("/{id}/comments")
    public ResponseEntity updateComment(@PathVariable Long id, @RequestBody CommentReqDto commentReqDto, @AuthenticationPrincipal CustomUserDetails user){
        ResponseDto responseDto = commentService.updateComment(id, commentReqDto, user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @DeleteMapping("/{id}/comments")
    public ResponseEntity deleteComment(@PathVariable Long id, @RequestBody CommentReqDto commentReqDto, @AuthenticationPrincipal CustomUserDetails user){
        String result = commentService.deleteComment(id, commentReqDto, user);
        int resultCode = "SUCCESS".equals(result) ? 1 : -1 ;

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ResponseDto<>(
                        resultCode,
                        result,
                        result,
                        CustomDateUtil.toStringFormat(LocalDateTime.now())
                ));
    }
}
