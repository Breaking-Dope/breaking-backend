package com.dope.breaking.api;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.FollowRepository;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.service.FollowService;
import com.dope.breaking.withMockCustomAuthorize.WithMockCustomUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RelationshipAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FollowService followService;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RelationshipAPI relationshipAPI;


    @BeforeEach //DB에 유저정보를 먼저 저장.
    public void createUserInfo() {

        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.USER) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);

    }

    @DisplayName("유저아이디가 유효한 경우, 팔로우가 정상적으로 동작한다.")
    @WithMockCustomUser
    @Test
    void follow() throws Exception{

        //Given
        User followedUser = new User();
        userRepository.save(followedUser);

        //When
        this.mockMvc.perform(post("/follow/{userId}",followedUser.getId()))
                .andExpect(status().isCreated()); //Then

        //Then
        assertTrue(followRepository.existsFollowsByFollowedAndFollowing(followedUser,userRepository.findByUsername("12345g").get()));
    }

    @DisplayName("이미 팔로우 중인 유저를 팔로우 할 경우, 예외가 발생한다.")
    @WithMockCustomUser
    @Test
    void followAlreadyFollowing() throws Exception{

        //Given
        User followedUser = new User();
        userRepository.save(followedUser);
        followService.follow("12345g",followedUser.getId());

        //When
        this.mockMvc.perform(post("/follow/{userId}",followedUser.getId()))
                .andExpect(status().isBadRequest()); //Then

    }

    @DisplayName("팔로우 중인 유저인 경우, 정상적으로 언팔로우 된다.")
    @WithMockCustomUser
    @Test
    void unfollow() throws Exception{

        //Given
        User followedUser = new User();
        userRepository.save(followedUser);
        followService.follow("12345g",followedUser.getId());

        //When
        this.mockMvc.perform(delete("/follow/{userId}",followedUser.getId()))
                .andExpect(status().isOk()); //Then

        //Then
        assertFalse(followRepository.existsFollowsByFollowedAndFollowing(followedUser,userRepository.findByUsername("12345g").get()));

    }

    @DisplayName("팔로우 하지 않은 유저를 언팔로우 할 경우, 예외가 발생한다.")
    @WithMockCustomUser
    @Test
    void unfollowAlreadyUnfollowingUser() throws Exception{

        //Given
        User followedUser = new User();
        userRepository.save(followedUser);

        //When
        this.mockMvc.perform(delete("/follow/{userId}",followedUser.getId()))
                .andExpect(status().isBadRequest()); //Then

    }


    @DisplayName("팔로잉 유저 리스트 반환 시, isFollowing 필드값이 정확히 반환된다.")
    @WithMockCustomUser
    @Test
    void followingUsersList() throws Exception {

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);
        User user3 = new User();
        user3.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username3", Role.USER);
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        followService.follow("username1",user2.getId());
        followService.follow("username1",user3.getId());
        followService.follow("12345g",user2.getId());

        //When
        ResultActions result = this.mockMvc.perform(get("/follow/following/{userId}", user1.getId()))
                .andExpect(status().isOk()) //Then
                .andExpect(jsonPath("$[0].isFollowing").value(true))
                .andExpect(jsonPath("$[1].isFollowing").value(false))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @DisplayName("팔로워 유저 리스트 반환 시, isFollowing 필드값이 정확히 반환된다.")
    @WithMockCustomUser
    @Test
    void followerUserList() throws Exception {

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);
        User user3 = new User();
        user3.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username3", Role.USER);
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        followService.follow("username1",user3.getId());
        followService.follow("username2",user3.getId());
        followService.follow("12345g",user1.getId());

        //When
        this.mockMvc.perform(get("/follow/follower/{userId}", user3.getId()))
                .andExpect(status().isOk()) //Then
                .andExpect(jsonPath("$[0].isFollowing").value(true))
                .andExpect(jsonPath("$[1].isFollowing").value(false))
                .andExpect(jsonPath("$", hasSize(2)));
    }

}