package com.dope.breaking.api;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.BookmarkRepository;
import com.dope.breaking.repository.PostLikeRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.service.BookmarkService;
import com.dope.breaking.service.PostLikeService;
import com.dope.breaking.withMockCustomAuthorize.WithMockCustomUser;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
class BookmarkAPITest {

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    public void createUserInfo() {
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.USER)
                .build();
        userRepository.save(user);
    }



    @DisplayName("로그인된 사용자가 북마크를 선택할 시, 북마크 기능이 정상적으로 작동한다.")
    @WithMockCustomUser
    @Test
    void bookmarkPost() throws Exception {
        //Given
        Post post = new Post();
        postRepository.save(post);

        //When
        this.mockMvc.perform(post("/post/{postId}/bookmark", post.getId()))
                .andExpect(status().isCreated()); //Then

        //Then
        Assertions.assertTrue(bookmarkRepository.existsByUserAndPost(userRepository.findByUsername("12345g").get(),post));
    }


    @DisplayName("이미 북마크한 사용자가 북마크를 선택할 시, 예외가 발생한다.")
    @WithMockCustomUser
    @Test
    void AlreadyBookmarkedPost() throws Exception {
        //Given
        Post post = new Post();
        postRepository.save(post);

        bookmarkService.bookmarkPost("12345g", post.getId());

        //When
        this.mockMvc.perform(post("/post/{postId}/bookmark", post.getId()))
                .andExpect(status().isBadRequest()); //Then

        //Then
    }




    @DisplayName("북마크한 사용자가 북마크를 해제하려 할 시, 정상적으로 해제된다.")
    @WithMockCustomUser
    @Test
    void unbookmarkPost() throws Exception {
        //Given
        Post post = new Post();
        postRepository.save(post);

        bookmarkService.bookmarkPost("12345g", post.getId());

        //When
        this.mockMvc.perform(delete("/post/{postId}/bookmark", post.getId()))
                .andExpect(status().isCreated()); //Then


        Assertions.assertFalse(bookmarkRepository.existsByUserAndPost(userRepository.findByUsername("12345g").get(), post));
    }



    @DisplayName("북마크를 하지 않은 사용자가 북마크를 해제하려 할 시, 예외가 반환된다.")
    @WithMockCustomUser
    @Test
    void AlreadyUnbookmarkedPost() throws Exception {
        //Given
        Post post = new Post();
        postRepository.save(post);


        //When
        this.mockMvc.perform(delete("/post/{postId}/bookmark", post.getId()))
                .andExpect(status().isBadRequest()); //Then

        //Then
    }
}