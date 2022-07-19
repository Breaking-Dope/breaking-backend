package com.dope.breaking.service;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ForListInfoResponseDto;
import com.dope.breaking.repository.UserRepository;
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
    @Autowired private UserRepository userRepository;


    @Test
    void isFollowing() {

        //Given
        User followingUser = new User();
        User followedUser = new User();

        //When
        followService.follow(followingUser,followedUser);

        //Then
        assertTrue(followService.isFollowing(followingUser,followedUser));

    }

    @Test
    void isNotFollowing() {

        //Given
        User followingUser = new User();
        User followedUser = new User();

        //When
        followService.follow(followingUser,followedUser);
        followService.unfollow(followingUser,followedUser);

        //Then
        assertFalse(followService.isFollowing(followingUser,followedUser));
    }


    @Test
    void Follow() {

        //Given
        User followingUser = new User();
        User followedUser = new User();

        //When
        followService.follow(followingUser,followedUser);

        //Then
        Assertions.assertThat(followingUser.getFollowingList().get(0).getFollowed()).isEqualTo(followedUser);
        Assertions.assertThat(followedUser.getFollowerList().get(0).getFollowing()).isEqualTo(followingUser);


    }

    @Test
    void Unfollow() {

        //Given
        User followingUser = new User();
        User followedUser = new User();

        //When
        followService.follow(followingUser,followedUser);
        followService.unfollow(followingUser,followedUser);

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
        followService.follow(followingUser,followedUser1);
        followService.follow(followingUser,followedUser2);

        userRepository.save(followingUser);
        userRepository.save(followedUser1);
        userRepository.save(followedUser2);

        List<ForListInfoResponseDto> followInfoResponseDtoList = followService.followingUsers(followingUser.getId());

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
        followService.follow(followingUser1,followedUser);
        followService.follow(followingUser2,followedUser);

        userRepository.save(followingUser1);
        userRepository.save(followingUser2);
        userRepository.save(followedUser);

        List<ForListInfoResponseDto> followInfoResponseDtoList = followService.followerUsers(followedUser.getId());


        //Then
        Assertions.assertThat(followInfoResponseDtoList.size()).isEqualTo(2);

    }

}