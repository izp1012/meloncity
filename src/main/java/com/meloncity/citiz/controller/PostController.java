package com.meloncity.citiz.controller;

import com.meloncity.citiz.dto.CustomUserDetails;
import com.meloncity.citiz.dto.PostReqDto;
import com.meloncity.citiz.dto.ResponseDto;
import com.meloncity.citiz.service.PostService;
import com.meloncity.citiz.util.CustomDateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 게시글 전체 조회
    @GetMapping
    public String getAllPosts(){
        postService.getPostAll();
        return "getAllPosts";
    }

    // 게시글 조회
    @GetMapping("/{id}")
    public String getPost(@PathVariable Long id){
        postService.getPost(id);
        return "getPost";
    }

    // 게시글 저장
    @PostMapping
    public ResponseEntity<ResponseDto<String>> savePost(@RequestBody PostReqDto postReqDto, @AuthenticationPrincipal CustomUserDetails user){
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
    @PostMapping("/{id}")
    public String updatePost(@PathVariable Long id, @RequestBody PostReqDto postReqDto){
//        System.out.println("getContent : " + postReqDto.getContent());
//        System.out.println("getTitle : " + postReqDto.getTitle());
//        System.out.println("postReqDto.getImgUrls() : " + postReqDto.getImgUrls().get(0));
//        System.out.println("postReqDto.getImgUrls() : " + postReqDto.getImgUrls().get(1));

        postReqDto.setPostId(id);
        postService.updatePost(postReqDto);

        return "updatePost";
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public String deletePost(@PathVariable Long id){
        postService.deletePost(id);
        return "updatePost";
    }
}
