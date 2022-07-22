package com.dope.breaking.api;

import com.dope.breaking.repository.PostRepository;
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
    private final PostRepository postRepository;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/{postId}/comment")
    public ResponseEntity addComment(@PathVariable Long postId, @RequestBody String content, Principal principal){

        commentService.addCommentToPost(postId, principal.getName(), content);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/reply/{commentId}")
    public ResponseEntity addReply(@PathVariable Long commentId, @RequestBody String content, Principal principal){

        commentService.addReplyToComment(commentId, principal.getName(), content);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

}
