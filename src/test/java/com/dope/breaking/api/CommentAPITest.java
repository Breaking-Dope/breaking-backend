package com.dope.breaking.api;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.CommentRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class CommentAPITest {

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

    @DisplayName("해당 제보가 존재할 경우, 댓글이 정상적으로 작성된다.")
    @WithMockCustomUser
    @Test
    @Transactional
    void addComment() throws Exception {

        //Given
        Post post = new Post();
        postRepository.save(post);
        String content = "comment";

        //When
        this.mockMvc.perform(post("/post/{postId}/comment",post.getId())
                        .content(content)
                        .contentType(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isCreated()); //Then

        //Then
        Assertions.assertThat(commentRepository.findAllByPost(postRepository.findById(post.getId()).get()).get(0).getContent()).isEqualTo("comment");
    }

    @DisplayName("해당 제보가 존재하지 않 경우, 댓글 작성시 예외가 발생한다.")
    @WithMockCustomUser
    @Test
    @Transactional
    void addCommentToNotExistingPost() throws Exception {

        //Given: an invalid postId, say 100L

        String content = "comment";

        //When
        this.mockMvc.perform(post("/post/{postId}/comment",100L)
                        .content(content)
                        .contentType(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isNotFound()); //Then

    }

    @DisplayName("해당 댓글이 존재할 경우, 대댓글이 정상적으로 작성된다.")
    @WithMockCustomUser
    @Test
    @Transactional
    void addReply() throws Exception {

        //Given
        Post post = new Post();
        postRepository.save(post);
        Long commentId = commentService.addComment(post.getId(),"12345g","hi");

        String content = "reply";

        //When
        this.mockMvc.perform(post("/post/reply/{commentId}", commentId)
                    .content(content)
                    .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isCreated());

        //Then
        Assertions.assertThat(commentRepository.findAllByPost(postRepository.findById(post.getId()).get()).get(1).getContent()).isEqualTo("reply");
    }

    @DisplayName("해당 댓글이 존재하지 않을 경우, 예외가 발생한다.")
    @WithMockCustomUser
    @Test
    @Transactional
    void addReplyToNotExistingComment() throws Exception {

        //Given: and invalid commentId, say 100L
        String content = "reply";

        //When
        this.mockMvc.perform(post("/post/reply/{commentId}", 100L)
                        .content(content)
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isNotFound()); //Then
    }

    @DisplayName("유저네임이 일치할 경우, 댓글이 수정된다.")
    @WithMockCustomUser
    @Test
    @Transactional
    void updateComment() throws Exception {

        //Given
        Post post = new Post();
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), "12345g","original");
        String content = "updated";

        //When
        this.mockMvc.perform(put("/post/comment/{commentId}", commentId)
                        .content(content)
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isCreated()); //Then

        //Then
        Assertions.assertThat(commentRepository.getById(commentId).getContent()).isEqualTo("updated");
    }

    @DisplayName("유저네임이 불일치할 경우, 예외가 발생한다")
    @WithMockCustomUser
    @Test
    @Transactional
    void updateCommentByAnotherUser() throws Exception {

        //Given
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);
        Post post = new Post();

        userRepository.save(user2);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), "username","original");
        String content = "updated";

        //When
        this.mockMvc.perform(put("/post/comment/{commentId}", commentId)
                        .content(content)
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isNotAcceptable()); //Then

    }

    @DisplayName("유저네임이 일치할 경우, 댓글이 삭제된다")
    @WithMockCustomUser
    @Test
    @Transactional
    void deleteComment() throws Exception {

        //Given
        Post post = new Post();
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), "12345g","hi");

        //When
        this.mockMvc.perform(delete("/post/comment/{commentId}", commentId))
                .andExpect(status().isOk()); //Then

        //Then
        Assertions.assertThat(commentRepository.findById(commentId).isEmpty()).isTrue();


    }

}