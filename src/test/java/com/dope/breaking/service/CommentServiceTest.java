package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.CommentRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentServiceTest {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CommentService commentService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private EntityManager entityManager;

    @DisplayName("제보가 존재할 경우, 댓글이 작성됩니다.")
    @Test
    @Transactional
    void addCommentToPost() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);
        Post post = new Post();

        userRepository.save(user);
        postRepository.save(post);

        //When
        commentService.addCommentToPost(post.getId(), user.getUsername(), "comment");

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertThat(commentRepository.count()).isEqualTo(1);

        commentRepository.deleteAll();

    }

    @DisplayName("댓글이 존재할 경우, 대댓글이 작성됩니다.")
    @Test
    @Transactional
    void addReplyToComment() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);
        Post post = new Post();
        userRepository.save(user);
        postRepository.save(post);
        Long commentId = commentService.addCommentToPost(post.getId(), user.getUsername(), "hi there");

        //When
        commentService.addCommentToPost(commentId, "username", "reply1");
        commentService.addCommentToPost(commentId, "username", "reply2");

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertThat(commentRepository.count()).isEqualTo(3);
        Assertions.assertThat(commentRepository.findAll().get(1).getContent()).isEqualTo("reply1");

    }
}