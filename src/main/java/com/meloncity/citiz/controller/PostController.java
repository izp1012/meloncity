package com.meloncity.citiz.controller;

import com.meloncity.citiz.dto.PostReqDto;
import com.meloncity.citiz.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
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
    public String savePost(@ModelAttribute PostReqDto postReqDto){
        System.out.println("getContent : " + postReqDto.getContent());
        System.out.println("getTitle : " + postReqDto.getTitle());
        System.out.println("postReqDto.getImgUrls() : " + postReqDto.getImgUrls().get(0));
        System.out.println("postReqDto.getImgUrls() : " + postReqDto.getImgUrls().get(1));

        postService.savePost(postReqDto);

        return "savePost";
    }

    // 게시글 변경
    @PostMapping("/{id}")
    public String updatePost(@PathVariable Long id, @ModelAttribute PostReqDto postReqDto){
        System.out.println("getContent : " + postReqDto.getContent());
        System.out.println("getTitle : " + postReqDto.getTitle());
        System.out.println("postReqDto.getImgUrls() : " + postReqDto.getImgUrls().get(0));
        System.out.println("postReqDto.getImgUrls() : " + postReqDto.getImgUrls().get(1));

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
