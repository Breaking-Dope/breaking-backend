package com.dope.breaking.service;

import com.dope.breaking.domain.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class FollowServiceTest {

    @Autowired private FollowService followService;

    @Autowired private UserService userService;

    @Autowired private EntityManager em;


    @Test
    void isFollowing() {

        //Given
        User followingUser = new User();
        User followedUser = new User();

        //When
        followService.AFollowB(followingUser,followedUser);

        //Then
        assertTrue(followService.isFollowing(followingUser,followedUser));

    }

    @Test
    void AFollowB() {

        //Given
        User followingUser = new User();
        User followedUser = new User();

        //When
        followService.AFollowB(followingUser,followedUser);

        //Then
        Assertions.assertThat(followingUser.getFollowingList().get(0).getFollowed()).isEqualTo(followedUser);
        Assertions.assertThat(followedUser.getFollowerList().get(0).getFollowing()).isEqualTo(followingUser);


    }

    @Test
    void AUnfollowB() {

        //Given
        User followingUser = new User();
        User followedUser = new User();

        //When
        followService.AFollowB(followingUser,followedUser);
        followService.AUnfollowB(followingUser,followedUser);

        //Then
        Assertions.assertThat(followingUser.getFollowingList().size()).isEqualTo(0);
        Assertions.assertThat(followedUser.getFollowerList().size()).isEqualTo(0);
    }
}