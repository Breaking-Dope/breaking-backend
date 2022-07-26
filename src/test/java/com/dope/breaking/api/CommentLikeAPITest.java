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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @DisplayName("세명의 유저가 댓글에 좋아요를 누른 경우, 리턴 된 리스트는 두명의 정보를 담는다.")
    @Transactional
    @Test
    void commentLikeList() throws Exception {

        //Given
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user3 = new User();
        user3.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);
        Post post = new Post();

        userRepository.save(user2);
        userRepository.save(user3);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), "12345g", "hi");

        CommentLike commentLike1 = new CommentLike(userRepository.findByUsername("12345g").get(),commentRepository.findById(commentId).get());
        CommentLike commentLike2 = new CommentLike(userRepository.getById(user2.getId()),commentRepository.getById(commentId));
        CommentLike commentLike3 = new CommentLike(userRepository.getById(user3.getId()),commentRepository.getById(commentId));

        commentLikeRepository.save(commentLike1);
        commentLikeRepository.save(commentLike2);
        commentLikeRepository.save(commentLike3);

        entityManager.flush();
        entityManager.clear();

        //When
        this.mockMvc.perform(get("/post/comment/{commentId}/like-list",commentId))
                .andExpect(status().isOk()) //Then
                .andExpect(jsonPath("$", hasSize(3)));

    }

    @DisplayName("아무도 해당 댓글을 좋아하지 않는 경우, 리턴 된 리스트의 길이는 0이다.")
    @Transactional
    @Test
    void commentLikeListWhenNobodyLiked() throws Exception{

        //Given
        Post post = new Post();
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), "12345g", "hi");
        entityManager.flush();
        entityManager.clear();

        //When
        this.mockMvc.perform(get("/post/comment/{commentId}/like-list",commentId))
                .andExpect(status().isOk()) //Then
                .andExpect(jsonPath("$", hasSize(0)));

    }

}
