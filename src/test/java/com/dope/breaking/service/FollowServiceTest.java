package com.dope.breaking.service;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ForListInfoResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.follow.AlreadyFollowingException;
import com.dope.breaking.exception.follow.AlreadyUnfollowingException;
import com.dope.breaking.exception.user.NoSuchUserException;
import com.dope.breaking.repository.FollowRepository;
import com.dope.breaking.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class FollowServiceTest {

    @Autowired private FollowService followService;
    @Autowired private UserRepository userRepository;
    @Autowired private FollowRepository followRepository;
    @Autowired private EntityManager entityManager;

    @DisplayName("팔로우 하고 있지 않은 경우, 정상적으로 팔로우 된다.")
    @Test
    void follow() {

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);
        userRepository.save(user1);
        userRepository.save(user2);

        //When
        followService.follow("username1",user2.getId());

        //Then
        assertTrue(followRepository.existsFollowsByFollowedAndFollowing(user2,user1));
    }

    @DisplayName("유저네임이 불일치 할 경우, 예외가 발생한다.")
    @Test
    void followWithWrongUsername(){

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);
        userRepository.save(user1);
        userRepository.save(user2);

        //When
        Assertions.assertThrows(InvalidAccessTokenException.class, ()
                ->  followService.follow("wrong username",user2.getId())); //Then
    }

    @DisplayName("유저아이디가 불일치 할 경우, 예외가 발생한다")
    @Test
    void followWithWrongUserId(){

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);
        userRepository.save(user1);
        userRepository.save(user2);

        //Then
        Assertions.assertThrows(NoSuchUserException.class, ()
                ->  followService.follow("username1",100L)); //When
    }

    @DisplayName("이미 팔로우 하고 있는 유저를 팔로우 할 경우, 예외가 발생한다.")
    @Test
    void followAlreadyFollowingUser(){

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);
        userRepository.save(user1);
        userRepository.save(user2);

        followService.follow("username1",user2.getId());

        //Then
        Assertions.assertThrows(AlreadyFollowingException.class, ()
                ->  followService.follow("username1",user2.getId())); //When

    }

    @DisplayName("팔로우 중인 유저를 언팔로우 할 경우, 언팔로우가 실행된다")
    @Test
    void unfollow(){

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);
        userRepository.save(user1);
        userRepository.save(user2);

        followService.follow("username1",user2.getId());

        //When
        followService.unfollow("username1",user2.getId());

        //Then
        followRepository.existsFollowsByFollowedAndFollowing(user2,user1);

    }

    @DisplayName("팔로우하고 있지 않은 유저를 언팔로우 할 경우, 예외가 발생한다.")
    @Test
    void unfollowAlreadyUnfollowingUser(){

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);
        userRepository.save(user1);
        userRepository.save(user2);


        //Then
        Assertions.assertThrows(AlreadyUnfollowingException.class, ()
                ->  followService.unfollow("username1",user2.getId())); //When

    }

}