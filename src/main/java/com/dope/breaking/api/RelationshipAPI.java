package com.dope.breaking.api;

import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.response.MessageResponseDto;
import com.dope.breaking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class RelationshipAPI {

    private final UserService userService;

    @PostMapping("/follow/{userId}")
    public ResponseEntity<?> followUser(@PathVariable Long userId) {

        // 1. username validation 을 시행한다.
        String username = "username";

        User followingUser = userService.findByUsername(username).get();

        // 2. userId 존재여부를 확인한다. (혹시라도 팔로우 하려는 사람이 탈퇴했으면?)
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

        // test 확인
        User checkedUser = userService.findById(userId).get();
        List<Follow> followerList = checkedUser.getFollowerList();
        for (Follow follow1 : followerList) {
            System.out.println(follow1.getFollowed().getNickname());

        }

        return ResponseEntity.ok().build();

    }


}
