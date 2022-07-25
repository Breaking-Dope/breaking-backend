package com.dope.breaking.api;

import com.dope.breaking.domain.comment.CommentLike;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.CommentLikeRepository;
import com.dope.breaking.repository.CommentRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.service.CommentLikeService;
import com.dope.breaking.service.CommentService;
import com.dope.breaking.withMockCustomAuthorize.WithMockCustomUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class CommentLikeAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void createUserInfo() {

        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.USER) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);

    }

    @DisplayName("댓글이 존재할 경우, 댓글 좋아요가 정상적으로 실행된다.")
    @WithMockCustomUser
    @Transactional
    @Test
    void likeComment() throws Exception {

        //Given
        Post post = new Post();
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), "12345g", "hi");

        entityManager.flush();
        entityManager.clear();

        //When
        this.mockMvc.perform(post("/post/comment/{commentId}/like", commentId))
                .andExpect(status().isCreated()); //Then

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertThat(commentLikeRepository.existsCommentLikeByUserAndComment(userRepository.findByUsername("12345g").get(), commentRepository.findById(commentId).get())).isTrue();
    }

    @DisplayName("이미 좋아요 한 댓글에 좋아요를 시도할 경우, 예외가 발생한다.")
    @WithMockCustomUser
    @Transactional
    @Test
    void likeAlreadyLikedComment() throws Exception {

        //Given
        Post post = new Post();
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), "12345g", "hi");
        CommentLike commentLike = new CommentLike(userRepository.findByUsername("12345g").get(), commentRepository.findById(commentId).get());
        commentLikeRepository.save(commentLike);

        entityManager.flush();
        entityManager.clear();

        //When
        this.mockMvc.perform(post("/post/comment/{commentId}/like", commentId))
                .andExpect(status().isBadRequest()); //Then

    }

    @DisplayName("이미 좋아요 한 댓글에 좋아요를 취소할 경우, 정상적으로 처리된다.")
    @WithMockCustomUser
    @Transactional
    @Test
    void unlikeComment() throws Exception {

        //Given
        Post post = new Post();
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), "12345g", "hi");
        CommentLike commentLike = new CommentLike(userRepository.findByUsername("12345g").get(), commentRepository.findById(commentId).get());
        commentLikeRepository.save(commentLike);

        entityManager.flush();
        entityManager.clear();

        //When
        this.mockMvc.perform(delete("/post/comment/{commentId}/like", commentId))
                .andExpect(status().isOk()); //Then

        Assertions.assertThat(commentLikeRepository.existsCommentLikeByUserAndComment(userRepository.findByUsername("12345g").get(), commentRepository.findById(commentId).get())).isFalse();

    }

    @DisplayName("이미 좋아요를 취소한 댓글에 좋아요를 취소할 경우, 정상적으로 처리된다.")
    @WithMockCustomUser
    @Transactional
    @Test
    void unlikeAlreadyUnlikedComment() throws Exception {

        //Given
        Post post = new Post();
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), "12345g", "hi");

        entityManager.flush();
        entityManager.clear();

        //When
        this.mockMvc.perform(delete("/post/comment/{commentId}/like", commentId))
                .andExpect(status().isBadRequest()); //Then

    }

}