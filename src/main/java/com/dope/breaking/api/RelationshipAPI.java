package com.dope.breaking.api;

import com.dope.breaking.dto.user.ForListInfoResponseDto;
import com.dope.breaking.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;


@RestController
@RequiredArgsConstructor
@Transactional
public class RelationshipAPI {

    private final FollowService followService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/follow/{userId}")
    public ResponseEntity followUser( Principal principal, @PathVariable Long userId) {

       followService.followUser(principal.getName(),userId);
       return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/follow/{userId}")
    public ResponseEntity unfollowUser(Principal principal, @PathVariable Long userId) {

       followService.unfollowUser(principal.getName(),userId);
       return ResponseEntity.ok().build();

    }

    @GetMapping("/follow/following/{userId}")
    public ResponseEntity<List<ForListInfoResponseDto>>followingUsers (@PathVariable Long userId) {

        return ResponseEntity.ok().body(followService.followingUsers(userId));

    }

    @GetMapping("/follow/follower/{userId}")
    public ResponseEntity<List<ForListInfoResponseDto>> followerUsers (@PathVariable Long userId){

        return ResponseEntity.ok().body(followService.followerUsers(userId));

    }

}
