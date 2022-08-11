package com.dope.breaking.repository;

import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ForListInfoResponseDto;
import com.dope.breaking.dto.user.SignUpRequestDto;
import com.dope.breaking.service.FollowTargetType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FollowRepositoryTest {

    @Autowired UserRepository userRepository;
    @Autowired FollowRepository followRepository;

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

        Follow follow1 = new Follow(hero,follower1);
        Follow follow2 = new Follow(hero,follower2);

        followRepository.save(follow1);
        followRepository.save(follow2);

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

    @DisplayName("타 유저의 팔로잉 명단을 조회시, 본인의 팔로우 여부가 정확히 반환된다. ")
    @Test
    void followingListByOtherUser() {

        //Given
        User hero = userRepository.findByNickname("hero").get();
        User followed1 = userRepository.findByNickname("follower1").get();
        User followed2 = userRepository.findByNickname("follower2").get();
        User followed3 = new User();
        User followed4 = new User();
        userRepository.save(followed3);
        userRepository.save(followed4);

        Follow follow3 = new Follow(hero, followed3);
        Follow follow4 = new Follow(hero, followed4);

        Follow follow5 = new Follow(followed1, followed3);
        Follow follow6 = new Follow(followed1, followed4);
        followRepository.save(follow3);
        followRepository.save(follow4);
        followRepository.save(follow5);
        followRepository.save(follow6);

        //When
        List<ForListInfoResponseDto> followingList = followRepository.followingList(followed1, hero,0L,10);

        //Then
        Assertions.assertEquals(4, followingList.size());
        Assertions.assertFalse(followingList.get(0).isFollowing());
        Assertions.assertFalse(followingList.get(1).isFollowing());
        Assertions.assertTrue(followingList.get(2).isFollowing());
        Assertions.assertTrue(followingList.get(3).isFollowing());

    }

    @DisplayName("로그인 안 한 사용자가 팔로잉 명단을 조회시, 팔로우 여부가 전부 false로 반환된다..")
    @Test
    void followingListByGuest() {

        //Given
        User hero = userRepository.findByNickname("hero").get();
        User followed1 = userRepository.findByNickname("follower1").get();
        User followed2 = userRepository.findByNickname("follower2").get();
        User followed3 = new User();
        User followed4 = new User();
        userRepository.save(followed3);
        userRepository.save(followed4);

        Follow follow3 = new Follow(hero, followed3);
        Follow follow4 = new Follow(hero, followed4);

        followRepository.save(follow3);
        followRepository.save(follow4);

        //When
        List<ForListInfoResponseDto> followingList = followRepository.followingList(null, hero,0L,10);

        //Then
        Assertions.assertEquals(4, followingList.size());
        Assertions.assertFalse(followingList.get(0).isFollowing());
        Assertions.assertFalse(followingList.get(1).isFollowing());
        Assertions.assertFalse(followingList.get(2).isFollowing());
        Assertions.assertFalse(followingList.get(3).isFollowing());

    }

    @DisplayName("사용자가 본인의 팔로잉 명단을 조회시, 팔로우 여부가 전부 true로 반환된다.")
    @Test
    void followingListByItself() {

        //Given
        User hero = userRepository.findByNickname("hero").get();
        User followed1 = userRepository.findByNickname("follower1").get();
        User followed2 = userRepository.findByNickname("follower2").get();
        User followed3 = new User();
        User followed4 = new User();
        userRepository.save(followed3);
        userRepository.save(followed4);

        Follow follow3 = new Follow(hero, followed3);
        Follow follow4 = new Follow(hero, followed4);

        followRepository.save(follow3);
        followRepository.save(follow4);

        //When
        List<ForListInfoResponseDto> followingList = followRepository.followingList(hero, hero,0L,10);

        //Then
        Assertions.assertEquals(4, followingList.size());
        Assertions.assertTrue(followingList.get(0).isFollowing());
        Assertions.assertTrue(followingList.get(1).isFollowing());
        Assertions.assertTrue(followingList.get(2).isFollowing());
        Assertions.assertTrue(followingList.get(3).isFollowing());

    }

    ////////////

    @DisplayName("타 유저의 팔로워 명단을 조회시, 본인의 팔로잉 여부가 정확히 반환된다.")
    @Test
    void followerListByOtherUser() {

        //Given
        User hero = userRepository.findByNickname("hero").get();
        User followingUser1 = userRepository.findByNickname("follower1").get();
        User followingUser2 = userRepository.findByNickname("follower2").get();
        Follow follow1 = new Follow(followingUser1, hero);
        Follow follow2 = new Follow(followingUser2, hero);
        Follow follow3 = new Follow(followingUser1, followingUser2);

        followRepository.save(follow1);
        followRepository.save(follow2);
        followRepository.save(follow3);

        User followingUser3 = new User();
        userRepository.save(followingUser3);

        Follow follow4 = new Follow(followingUser3, hero);
        followRepository.save(follow4);


        //When
        List<ForListInfoResponseDto> followerList = followRepository.followerList(followingUser1, hero,0L,10);

        //Then
        Assertions.assertEquals(3, followerList.size());
        Assertions.assertFalse(followerList.get(0).isFollowing());
        Assertions.assertTrue(followerList.get(1).isFollowing());
        Assertions.assertFalse(followerList.get(2).isFollowing());


    }

    @DisplayName("로그인 안 한 사용자가 팔로워 명단을 조회시, 팔로우 여부가 전부 false로 반환된다.")
    @Test
    void followerListByGuest() {

        //Given
        User hero = userRepository.findByNickname("hero").get();
        User followingUser1 = userRepository.findByNickname("follower1").get();
        User followingUser2 = userRepository.findByNickname("follower2").get();
        Follow follow1 = new Follow(followingUser1, hero);
        Follow follow2 = new Follow(followingUser2, hero);
        Follow follow3 = new Follow(followingUser1, followingUser2);

        followRepository.save(follow1);
        followRepository.save(follow2);
        followRepository.save(follow3);

        User followingUser3 = new User();
        userRepository.save(followingUser3);

        Follow follow4 = new Follow(followingUser3, hero);
        followRepository.save(follow4);


        //When
        List<ForListInfoResponseDto> followerList = followRepository.followerList(null, hero,0L,10);

        //Then
        Assertions.assertEquals(3, followerList.size());
        Assertions.assertFalse(followerList.get(0).isFollowing());
        Assertions.assertFalse(followerList.get(1).isFollowing());
        Assertions.assertFalse(followerList.get(2).isFollowing());

    }

    @DisplayName("사용자가 본인의 팔로워 명단을 조회시, 팔로우 여부가 정확히 반환된다.")
    @Test
    void followerListByItself() {

        //Given
        User hero = userRepository.findByNickname("hero").get();
        User followingUser1 = userRepository.findByNickname("follower1").get();
        User followingUser2 = userRepository.findByNickname("follower2").get();
        Follow follow1 = new Follow(followingUser1, hero);
        Follow follow2 = new Follow(followingUser2, hero);
        Follow follow3 = new Follow(followingUser1, followingUser2);

        followRepository.save(follow1);
        followRepository.save(follow2);
        followRepository.save(follow3);

        User followingUser3 = new User();
        userRepository.save(followingUser3);

        Follow follow4 = new Follow(followingUser3, hero);
        followRepository.save(follow4);

        //When
        List<ForListInfoResponseDto> followingList = followRepository.followerList(hero, hero,0L,10);

        for (ForListInfoResponseDto forListInfoResponseDto : followingList) {
            System.out.println(forListInfoResponseDto);
        }
        //Then
        Assertions.assertEquals(3, followingList.size());
        Assertions.assertTrue(followingList.get(0).isFollowing());
        Assertions.assertTrue(followingList.get(1).isFollowing());
        Assertions.assertFalse(followingList.get(2).isFollowing());

    }

}