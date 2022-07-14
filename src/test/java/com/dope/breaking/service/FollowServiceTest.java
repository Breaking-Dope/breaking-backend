package com.dope.breaking.service;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.FollowInfoResponseDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class FollowServiceTest {

    @Autowired private FollowService followService;
    @Autowired private UserService userService;


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
    void isNotFollowing() {

        //Given
        User followingUser = new User();
        User followedUser = new User();

        //When
        followService.AFollowB(followingUser,followedUser);
        followService.AUnfollowB(followingUser,followedUser);

        //Then
        assertFalse(followService.isFollowing(followingUser,followedUser));
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

    @Test
    void followingUsers(){

        //Given
        User followingUser = new User();
        User followedUser1 = new User();
        User followedUser2 = new User();

        //When
        followService.AFollowB(followingUser,followedUser1);
        followService.AFollowB(followingUser,followedUser2);

        userService.save(followingUser);
        userService.save(followedUser1);
        userService.save(followedUser2);

        List<FollowInfoResponseDto> followInfoResponseDtoList = followService.followingUsers(followingUser.getId());

        //Then
        Assertions.assertThat(followInfoResponseDtoList.size()).isEqualTo(2);

    }

    @Test
    void followerUsers(){

        //Given
        User followingUser1 = new User();
        User followingUser2 = new User();
        User followedUser = new User();

        //When
        followService.AFollowB(followingUser1,followedUser);
        followService.AFollowB(followingUser2,followedUser);

        userService.save(followingUser1);
        userService.save(followingUser2);
        userService.save(followedUser);

        List<FollowInfoResponseDto> followInfoResponseDtoList = followService.followerUsers(followedUser.getId());


        //Then
        Assertions.assertThat(followInfoResponseDtoList.size()).isEqualTo(2);

    }

}