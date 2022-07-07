package com.dope.breaking.api;

import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.PostResType;
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
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Transactional
public class RelationshipAPI {

    private final UserService userService;
    private final FollowService followService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/follow/{userId}")
    public ResponseEntity<?> followUser(Principal principal, @PathVariable Long userId) {

        // 1. username validation 을 시행한다.
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponseDto(PostResType.NOT_FOUND_USER.getMessage()));
        }//유저 정보 없으면 일치하지 않다고 반환하기.
        if (!userService.existByUsername(principal.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDto(PostResType.NOT_REGISTERED_USER.getMessage()));
        }

        User followingUser = userService.findByUsername(principal.getName()).get();

        // 2. userId 존재여부를 확인한다.
        Optional<User> user = userService.findById(userId);

        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("invalid user Id"));
        }

        User followedUser = user.get();

        // 3. 이미 follow가 된 상태인지 검사한다.
        List<Follow> followingList = followingUser.getFollowingList();
        for (Follow follow : followingList) {
            if (follow.getFollowed().getId() == userId){
                return ResponseEntity.badRequest().body(new MessageResponseDto("following already"));
            }
        }

        // 4. Follow 객체 생성 후 추가
        Follow follow = new Follow();
        followingUser.addFollowing(follow, followedUser);

        userService.save(followingUser);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/follow/{userId}")
    public ResponseEntity<?> unfollowUser(Principal principal, @PathVariable Long userId) {

        // 1. username validation 을 시행한다.
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponseDto(PostResType.NOT_FOUND_USER.getMessage()));
        }//유저 정보 없으면 일치하지 않다고 반환하기.
        if (!userService.existByUsername(principal.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDto(PostResType.NOT_REGISTERED_USER.getMessage()));
        }
        User followingUser = userService.findByUsername(principal.getName()).get();

        // 2. userId 존재여부를 확인한다.
        Optional<User> user = userService.findById(userId);

        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("invalid user Id"));
        }

        // 3. 팔로우 중인 유저인지 확인한다.
        User followedUser = user.get();

        List<Follow> followingList = followingUser.getFollowingList();

        Follow toRemoveFollow = null;

        Boolean following = false;
        for (Follow follow : followingList) {
            if (follow.getFollowed() == followedUser){
                toRemoveFollow = follow;
                following = true;
            }
        }

        if (following == false){
            return ResponseEntity.badRequest().body(new MessageResponseDto("already not following the user"));
        }

        // 4. follow 를 삭제한다.
        followingList.remove(toRemoveFollow);
        followedUser.getFollowerList().remove(toRemoveFollow);
        followService.deleteById(toRemoveFollow.getId());

        return ResponseEntity.ok().build();
    }

}
