package com.dope.breaking.service;

import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.domain.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FollowServiceTest {

    @Autowired private FollowService followService;

    @Autowired private UserService userService;

    @Test
    public void followCheck(){

        //Given
        User followingUser = new User();
        User followedUser = new User();

        //When
        Follow follow = new Follow();
        followingUser.addFollowing(follow, followedUser);

        //Then
        Assertions.assertThat(followingUser.getFollowingList().size()).isEqualTo(1);

    }

}