package com.dope.breaking.api;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.response.MessageResponseDto;
import com.dope.breaking.service.FollowService;
import com.dope.breaking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Objects;


@RestController
@RequiredArgsConstructor
@Transactional
public class RelationshipAPI {

    private final UserService userService;
    private final FollowService followService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/follow/{userId}")
    public ResponseEntity<?> followUser(Principal principal, @PathVariable Long userId) {

        User followingUser = userService.findByUsername(principal.getName()).get();

        // 2. userId 존재여부를 확인한다.
        if(!userService.existById(userId)){
            return ResponseEntity.badRequest().body(new MessageResponseDto("invalid user Id"));
        }
        User followedUser = userService.findById(userId).get();

        // 3. 이미 follow가 된 상태인지 검사한다.
        if (followService.isFollowing(followingUser,followedUser)){
            return ResponseEntity.badRequest().body(new MessageResponseDto("following already"));
        }

        // 4. Follow 객체 생성 후 추가
        followService.AFollowB(followingUser,followedUser);
        userService.save(followingUser);
        userService.save(followedUser);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/follow/{userId}")
    public ResponseEntity<?> unfollowUser(Principal principal, @PathVariable Long userId) {

        User followingUser = userService.findByUsername(principal.getName()).get();

        // 2. userId 존재여부를 확인한다.
        if(!userService.existById(userId)){
            return ResponseEntity.badRequest().body(new MessageResponseDto("invalid user Id"));
        }
        User followedUser = userService.findById(userId).get();

        // 3. 이미 unfollow가 된 상태인지 검사한다.
        if (!followService.isFollowing(followingUser,followedUser)){
            return ResponseEntity.badRequest().body(new MessageResponseDto("unfollowing already"));
        }

        // 4. follow 를 삭제한다.
        followService.AUnfollowB(followingUser,followedUser);
        return ResponseEntity.ok().build();

    }

}
