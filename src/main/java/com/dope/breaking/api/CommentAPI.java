package com.dope.breaking.api;

import com.dope.breaking.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentAPI {

    private final CommentService commentService;

    @PostMapping("/post/{postId}/comment")
    public ResponseEntity addComment(@PathVariable Long postId, @RequestParam String content){

        commentService.addCommentToPost(postId,"username",content);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PostMapping("/post/{postId}/comment/{commentId}")
    public ResponseEntity addReply(@PathVariable Long postId, @PathVariable Long commentId, @RequestParam String content){

        commentService.addCommentToPost(postId,"username",content);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

}
