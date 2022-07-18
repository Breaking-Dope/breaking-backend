package com.dope.breaking.api;

import com.dope.breaking.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
public class PostLikeAPI {

    private final PostLikeService postLikeService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/like/{postId}")
    public ResponseEntity likePostById(Principal principal, @PathVariable Long postId){
        postLikeService.likePostById(principal.getName(), postId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/post/like/{postId}")
    public ResponseEntity unlikePostById(Principal principal, @PathVariable Long postId){
        postLikeService.unlikePostById(principal.getName(),postId);
        return ResponseEntity.ok().build();
    }

}
