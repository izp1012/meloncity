package com.meloncity.citiz.controller;

import com.meloncity.citiz.dto.CommentReqDto;
import com.meloncity.citiz.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public String getComments(@ModelAttribute CommentReqDto commentReqDto){
        commentService.getComments(commentReqDto.getPostId());

        return "getComments";
    }

    @PostMapping
    public String saveComment(@ModelAttribute CommentReqDto commentReqDto){
        System.out.println("postid : " + commentReqDto.getPostId());
        System.out.println("comment : " + commentReqDto.getContent());
        commentService.saveComment(commentReqDto);

        return "saveComment";
    }

    @PostMapping("/{id}")
    public String updateComment(@ModelAttribute CommentReqDto commentReqDto){
        commentService.updateComment(commentReqDto);

        return "updateComment";
    }

    @DeleteMapping("/{id}")
    public String deleteComment(@PathVariable Long id){

        commentService.deleteComment(id);

        return "deleteComment";
    }
}
