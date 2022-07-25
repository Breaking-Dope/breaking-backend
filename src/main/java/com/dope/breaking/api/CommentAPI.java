package com.dope.breaking.api;

import com.dope.breaking.repository.CommentRepository;
import com.dope.breaking.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class CommentAPI {

    private final CommentService commentService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/{postId}/comment")
    public ResponseEntity addComment(@PathVariable Long postId, @RequestBody String content, Principal principal){

        commentService.addComment(postId, principal.getName(), content);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/comment/{commentId}/reply")
    public ResponseEntity addReply(@PathVariable Long commentId, @RequestBody String content, Principal principal){

        commentService.addReply(commentId, principal.getName(), content);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping ("/post/comment/{commentId}")
    public ResponseEntity updateCommentAndReply(@PathVariable Long commentId, @RequestBody String content, Principal principal){

        commentService.updateCommentOrReply(principal.getName(), commentId, content);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping ("/post/comment/{commentId}")
    public ResponseEntity deleteCommentAndReply(@PathVariable Long commentId, Principal principal){

        commentService.deleteCommentOrReply(principal.getName(), commentId);
        return ResponseEntity.ok().build();

    }
    
}
