package com.dope.breaking.api;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.PostLikeRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.service.PostLikeService;
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
class PostLikeAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach //DB에 유저정보를 먼저 저장.
    public void createUserInfo() {

        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.USER) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);

    }

    @AfterEach
    public void afterCleanUp() {
        userRepository.deleteAll();
        postRepository.deleteAll();
    }

    @WithMockCustomUser
    @Test
    void likePostById() throws Exception{

        //Given
        Post post = new Post();
        postRepository.save(post);

        //When
        this.mockMvc.perform(post("/post/{postId}/like",post.getId()))
                .andExpect(status().isCreated()); //Then

        //Then
        Assertions.assertThat(postLikeRepository.existsPostLikesByUserAndPost(userRepository.findByUsername("12345g").get(),post)).isTrue();
    }

    @WithMockCustomUser
    @Test
    void likePostByIdWhenHasLiked() throws Exception{

        //Given
        Post post = new Post();
        postRepository.save(post);

        postLikeService.likePost(userRepository.findByUsername("12345g").get(),post);

        //When
        this.mockMvc.perform(post("/post/{postId}/like",post.getId()))
                .andExpect(status().isBadRequest()); //Then

    }


    @WithMockCustomUser
    @Test
    void unlikePostById() throws Exception{

        //Given
        Post post = new Post();
        postRepository.save(post);

        postLikeService.likePost(userRepository.findByUsername("12345g").get(),post);

        //When
        this.mockMvc.perform(delete("/post/{postId}/like",post.getId()))
                .andExpect(status().isOk()); //Then

        //Then
        Assertions.assertThat(postLikeRepository.existsPostLikesByUserAndPost(userRepository.findByUsername("12345g").get(),post)).isFalse();
    }

    @WithMockCustomUser
    @Test
    void unlikePostByIdWhenHasUnliked() throws Exception{

        //Given
        Post post = new Post();
        postRepository.save(post);

        //When
        this.mockMvc.perform(delete("/post/{postId}/like",post.getId()))
                .andExpect(status().isBadRequest()); //Then

    }

    @Test
    void likedUserList() throws Exception{

        //Given
        User user1 = new User();
        User user2 = new User();
        Post post = new Post();

        userRepository.save(user1);
        userRepository.save(user2);
        postRepository.save(post);

        postLikeService.likePost(user1,post);
        postLikeService.likePost(user2,post);

        //When
        this.mockMvc.perform(get("/post/{postId}/like-list",post.getId()))
                .andExpect(status().isOk()) //Then
                .andExpect(jsonPath("$", hasSize(2)));
    }

}