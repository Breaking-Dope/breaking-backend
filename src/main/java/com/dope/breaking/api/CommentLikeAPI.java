package com.dope.breaking.api;

import com.dope.breaking.dto.user.ForListInfoResponseDto;
import com.dope.breaking.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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

    @GetMapping("/post/comment/{commentId}/like-list")
    public ResponseEntity<List<ForListInfoResponseDto>> commentLikedUserList(Principal principal, @PathVariable Long commentId){

        return ResponseEntity.ok().body(commentLikeService.commentLikeList(principal,commentId));

    }

}
