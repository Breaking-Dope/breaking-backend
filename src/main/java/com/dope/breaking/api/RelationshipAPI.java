package com.dope.breaking.api;

import com.dope.breaking.dto.user.ForListInfoResponseDto;
import com.dope.breaking.service.FollowService;
import com.dope.breaking.service.FollowTargetType;
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

       followService.follow(principal.getName(),userId);
       return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/follow/{userId}")
    public ResponseEntity unfollowUser(Principal principal, @PathVariable Long userId) {

       followService.unfollow(principal.getName(),userId);
       return ResponseEntity.ok().build();

    }

    @GetMapping("/follow/following/{userId}")
    public ResponseEntity<List<ForListInfoResponseDto>> followingUserList (
            Principal principal,
            @PathVariable Long userId,
            @RequestParam(value = "cursor") Long cursorId,
            @RequestParam(value = "size") int size) {

        return ResponseEntity.ok().body(followService.followUserList(principal, userId, cursorId, size, FollowTargetType.FOLLOWING));

    }

    @GetMapping("/follow/follower/{userId}")
    public ResponseEntity<List<ForListInfoResponseDto>> followerUserList (
            Principal principal,
            @PathVariable Long userId,
            @RequestParam(value = "cursor") Long cursorId,
            @RequestParam(value = "size") int size) {

        return ResponseEntity.ok().body(followService.followUserList(principal, userId, cursorId, size, FollowTargetType.FOLLOWED));

    }

}
