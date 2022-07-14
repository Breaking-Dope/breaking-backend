package com.dope.breaking.api;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.FollowRepository;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.service.FollowService;
import com.dope.breaking.service.UserService;
import com.dope.breaking.withMockCustomAuthorize.WithMockCustomUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private UserService userService;

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

        userService.save(user);

    }

    @AfterEach
    public void afterCleanUp() {
        userRepository.deleteAll();
        followRepository.deleteAll();
    }

    @WithMockCustomUser
    @Test
    void followUser() throws Exception{

        //Given
        User followedUser = new User();
        userService.save(followedUser);

        //When
        this.mockMvc.perform(post("/follow/{userId}",followedUser.getId()))
                .andExpect(status().isCreated()); //Then

        //Then
        Assertions.assertThat(followedUser.getFollowerList().size()).isEqualTo(1);
        Assertions.assertThat(userService.findById(1L).get().getFollowingList().size()).isEqualTo(1);

    }

    @WithMockCustomUser
    @Test
    void followUserThatNeverExists() throws Exception{

        // Given: No user with the primary key value of "2L" in userRepository

        //When
        this.mockMvc.perform(post("/follow/{userId}",2L))
                .andExpect(status().isNotFound() ); //Then

    }

    @WithMockCustomUser
    @Test
    public void followUserThatIsAlreadyFollowing() throws Exception{

        //Given
        User followingUser = userService.findByUsername("12345g").get();
        User followedUser = new User();
        userService.save(followedUser);

        //When
        followService.AFollowB(followingUser,followedUser);
        userService.save(followingUser);
        userService.save(followedUser);

        //Then
        this.mockMvc.perform(post("/follow/{userId}",followedUser.getId()))
                .andExpect(status().isBadRequest() );

    }


    @WithMockCustomUser
    @Test
    public void unfollowUser() throws Exception{

        //Given
        User followingUser = userService.findByUsername("12345g").get();
        User followedUser = new User();

        followService.AFollowB(followingUser,followedUser);
        userService.save(followedUser);

        //When
        this.mockMvc.perform(delete("/follow/{userId}", followedUser.getId()))
                .andExpect(status().isOk());

        //Then
        Assertions.assertThat(followingUser.getFollowingList().size()).isEqualTo(0);
        Assertions.assertThat(followedUser.getFollowerList().size()).isEqualTo(0);

    }

    @WithMockCustomUser
    @Test
    public void unfollowUserThatNeverExists() throws Exception{

        //Given no user with userId 10L

        //When
        this.mockMvc.perform(delete("/follow/{userId}", 10L))
                .andExpect(status().isNotFound()); //Then
    }

    @WithMockCustomUser
    @Test
    void unfollowUserThatIsAlreadyUnfollowing() throws Exception{

        //Given
        User notFollowedUser  = new User();
        userService.save(notFollowedUser);

        //When
        this.mockMvc.perform(delete("/follow/{userId}",notFollowedUser.getId()))
                .andExpect(status().isBadRequest()); //Then

    }

    @Test
    void followingUsersList() throws Exception {

        //Given
        User followingUser = new User();
        User followedUser1 = new User();
        User followedUser2 = new User();

        followService.AFollowB(followingUser,followedUser1);
        followService.AFollowB(followingUser,followedUser2);

        userService.save(followingUser);
        userService.save(followedUser1);
        userService.save(followedUser2);

        //When
         this.mockMvc.perform(get("/follow/following/{userId}",followingUser.getId()))
                 .andExpect(status().isOk()) //Then
                 .andExpect(jsonPath("$", hasSize(2)));

    }

    @Test
    void followingUsersListWhenFollowingNobody() throws Exception {

        //Given
        User user = new User();
        userService.save(user);

        //When
        this.mockMvc.perform(get("/follow/following/{userId}",user.getId()))
                .andExpect(status().isOk()) //Then
                .andExpect(jsonPath("$",hasSize(0)));

    }

    @Test
    void followerUsersList() throws Exception {

        // Given
        User followingUser1 = new User();
        User followingUser2 = new User();
        User followedUser = new User();

        followService.AFollowB(followingUser1,followedUser);
        followService.AFollowB(followingUser2,followedUser);

        userService.save(followingUser1);
        userService.save(followingUser2);
        userService.save(followedUser);

        //When
        this.mockMvc.perform(get("/follow/follower/{userId}",followedUser.getId()))
                .andExpect(status().isOk()) //Then
                .andExpect(jsonPath("$",hasSize(2)));

    }

}