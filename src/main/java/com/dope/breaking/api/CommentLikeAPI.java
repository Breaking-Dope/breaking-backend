package com.dope.breaking.api;

import com.dope.breaking.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class CommentLikeAPI {

    private final CommentLikeService commentLikeService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/comment/{commentId}/like")
    public ResponseEntity likeComment(@PathVariable Long commentId, Principal principal){

        commentLikeService.likeComment(principal.getName(), commentId);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/post/comment/{commentId}/like")
    public ResponseEntity unlikeComment(@PathVariable Long commentId, Principal principal){

        commentLikeService.unlikeComment(principal.getName(), commentId);
        return ResponseEntity.ok().build();

    }

}
