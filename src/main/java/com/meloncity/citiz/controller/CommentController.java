package com.meloncity.citiz.controller;

import com.meloncity.citiz.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

//    @GetMapping
//    public String getComments(@ModelAttribute CommentDto commentDto){
//        commentService.getComments(commentDto.getPostId());
//
//        return "getComments";
//    }
//
//    @PostMapping
//    public String saveComment(@ModelAttribute CommentDto commentDto){
//        System.out.println("postid : " + commentDto.getPostId());
//        System.out.println("comment : " + commentDto.getContent());
//        commentService.saveComment(commentDto);
//
//        return "saveComment";
//    }
//
//    @PostMapping("/{id}")
//    public String updateComment(@ModelAttribute CommentDto commentDto){
//        commentService.updateComment(commentDto);
//
//        return "updateComment";
//    }
//
//    @DeleteMapping("/{id}")
//    public String deleteComment(@PathVariable Long id){
//
//        commentService.deleteComment(id);
//
//        return "deleteComment";
//    }
}
