package com.dope.breaking.api;

import com.dope.breaking.dto.user.ForListInfoResponseDto;
import com.dope.breaking.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class PostLikeAPI {

    private final PostLikeService postLikeService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/{postId}/like")
    public ResponseEntity<Void> likePostById(Principal principal, @PathVariable Long postId){
        postLikeService.likePostById(principal.getName(), postId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/post/{postId}/like")
    public ResponseEntity<Void> unlikePostById(Principal principal, @PathVariable Long postId){
        postLikeService.unlikePostById(principal.getName(),postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("post/{postId}/like-list")
    public ResponseEntity<List<ForListInfoResponseDto>> likedUserList (
            Principal principal,
            @PathVariable Long postId,
            @RequestParam(value = "cursor") Long cursorId,
            @RequestParam(value = "size") int size
    ) {
        String username =  null;
        if(principal != null){ username = principal.getName(); }
        return ResponseEntity.ok().body(postLikeService.postLikeList(username,postId,cursorId,size));
    }

}
