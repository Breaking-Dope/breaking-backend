package com.dope.breaking.repository;

import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.SearchUserResponseDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired UserRepository userRepository;
    @Autowired FollowRepository followRepository;
    @Autowired EntityManager em;

    @Test
    void findByNickname() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);
        userRepository.save(user);

        //When
        User foundUser = userRepository.findByNickname(user.getNickname()).get();

        //Then
        assertEquals(user, foundUser);

    }


    @Test
    void findByEmail() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username",Role.USER);
        userRepository.save(user);

        //When
        User foundUser = userRepository.findByEmail(user.getEmail()).get();

        //Then
        assertEquals(user, foundUser);

    }

    @DisplayName("유저 닉네임을 pagination으로 검색한다.")
    @Test
    void searchUserByUserNickname() {

        User user1 = new User();
        user1.setRequestFields("URL", "anyURL", "테스트닉네임1", "01012345678", "abc.google.com", "realname", "msg", "username", Role.USER);
        userRepository.save(user1);

        User user2 = new User();
        user2.setRequestFields("URL", "anyURL", "테스트닉네임2", "01012345678", "abc.google.com", "realname", "msg", "username", Role.USER);
        userRepository.save(user2);

        User user3 = new User();
        user3.setRequestFields("URL", "anyURL", "테스트닉네임3", "01012345678", "abc.google.com", "realname", "msg", "username", Role.USER);
        userRepository.save(user3);

        em.flush();

        List<SearchUserResponseDto> result1 = userRepository.searchUserBy(null, "테스트닉네임", null, 2L);

        User cursorUser = userRepository.findById(result1.get(1).getUserId()).get();
        List<SearchUserResponseDto> result2 = userRepository.searchUserBy(null, "테스트닉네임", cursorUser, 2L);

        assertEquals(2, result1.size());
        assertEquals(1, result2.size());

        assertEquals(user3.getId(), result2.get(0).getUserId());

    }

    @DisplayName("유저 검색 시, 팔로워중인 사람의 수가 표시된다.")
    @Test
    void SearchedUserFollowerCount() {

        User user = new User();
        user.setRequestFields("URL", "anyURL", "닉네임", "01012345678", "abc.google.com", "realname", "msg", "username", Role.USER);
        userRepository.save(user);

        followRepository.save(new Follow(user, user));
        followRepository.save(new Follow(user, user));
        followRepository.save(new Follow(user, user));
        followRepository.save(new Follow(user, user));
        followRepository.save(new Follow(user, user));

        em.flush();

        List<SearchUserResponseDto> result = userRepository.searchUserBy(null, "닉네임", null, 5L);

        assertEquals(5, result.get(0).getFollowerCount());

    }

}