package com.dope.breaking.repository;

import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.SignUpRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FollowRepositoryTest {

    @Autowired UserRepository userRepository;
    @Autowired FollowRepository followRepository;
    @Autowired EntityManager em;

    @BeforeEach //DB에 유저정보를 먼저 저장.
    public void createUserInfo() {

        User hero = new User();
        SignUpRequestDto signUpRequest =  new SignUpRequestDto
                ("statusMsg","hero","phoneNumber","test@email.com","realname","testUsername", "press");
        hero.setRequestFields(
                "anyURL",
                "anyURL",
                signUpRequest.getNickname(),
                signUpRequest.getPhoneNumber(),
                signUpRequest.getEmail(),
                signUpRequest.getRealName(),
                signUpRequest.getStatusMsg(),
                signUpRequest.getUsername(),
                Role.valueOf(signUpRequest.getRole().toUpperCase(Locale.ROOT))
        );
        userRepository.save(hero);

        User follower1 = new User();
        SignUpRequestDto signUpRequest2 =  new SignUpRequestDto
                ("statusMsg","follower1","phoneNumber","test@email.com","realname","testUsername", "press");
        follower1.setRequestFields(
                "anyURL",
                "anyURL",
                signUpRequest2.getNickname(),
                signUpRequest2.getPhoneNumber(),
                signUpRequest2.getEmail(),
                signUpRequest2.getRealName(),
                signUpRequest2.getStatusMsg(),
                signUpRequest2.getUsername(),
                Role.valueOf(signUpRequest2.getRole().toUpperCase(Locale.ROOT))
        );
        userRepository.save(follower1);

        User follower2 = new User();
        SignUpRequestDto signUpRequest3 =  new SignUpRequestDto
                ("statusMsg","follower2","phoneNumber","test@email.com","realname","testUsername", "press");
        follower2.setRequestFields(
                "anyURL",
                "anyURL",
                signUpRequest3.getNickname(),
                signUpRequest3.getPhoneNumber(),
                signUpRequest3.getEmail(),
                signUpRequest3.getRealName(),
                signUpRequest3.getStatusMsg(),
                signUpRequest3.getUsername(),
                Role.valueOf(signUpRequest3.getRole().toUpperCase(Locale.ROOT))
        );
        userRepository.save(follower2);

        Follow follow1 = new Follow();

        follow1.updateFollowing(hero);
        follow1.updateFollowed(follower1);
        follower1.getFollowingList().add(follow1);
        hero.getFollowerList().add(follow1);

        // ==================

        Follow follow2 = new Follow();

        follow2.updateFollowing(hero);
        follow2.updateFollowed(follower2);

        follower2.getFollowingList().add(follow2);
        hero.getFollowerList().add(follow2);

    }

    @AfterEach
    public void afterCleanUp() {
        userRepository.deleteAll();
        followRepository.deleteAll();
    }

    @Test
    void countFollowerAndFollowing() {

        User hero = userRepository.findByNickname("hero").get();
        User follower1 = userRepository.findByNickname("follower1").get();
        User follower2 = userRepository.findByNickname("follower2").get();


        assertEquals(2, followRepository.countFollowsByFollowing(hero));
        assertEquals(0, followRepository.countFollowsByFollowed(hero));

        assertEquals(1, followRepository.countFollowsByFollowed(follower1));
        assertEquals(1, followRepository.countFollowsByFollowed(follower2));

        assertEquals(0, followRepository.countFollowsByFollowing(follower1));
        assertEquals(0, followRepository.countFollowsByFollowing(follower2));
    }

    @Test
    void existsFollowsByFollowedAndFollowing() {

        User hero = userRepository.findByNickname("hero").get();
        User follower1 = userRepository.findByNickname("follower1").get();
        User follower2 = userRepository.findByNickname("follower2").get();

        assertTrue(followRepository.existsFollowsByFollowedAndFollowing(follower1, hero), () -> "hero 은 follower1 를 팔로우 중입니다.");
        assertTrue(followRepository.existsFollowsByFollowedAndFollowing(follower2, hero), () -> "follower2 은 hero 를 팔로우 중입니다.");
        assertFalse(followRepository.existsFollowsByFollowedAndFollowing(hero, follower1), () -> "hero 은 hero 를 팔로우 하지 않습니다.");
        assertFalse(followRepository.existsFollowsByFollowedAndFollowing(follower1, follower2), () -> "follower1 은 follower2 를 팔로우 하지 않습니다.");
    }
}