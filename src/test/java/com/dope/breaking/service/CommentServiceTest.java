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

    @DisplayName("제보가 존재할 경우, 댓글이 작성된다.")
    @Test
    @Transactional
    void addComment() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);
        Post post = new Post();

        userRepository.save(user);
        postRepository.save(post);

        //When
        commentService.addComment(post.getId(), user.getUsername(), "comment");

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertThat(commentRepository.count()).isEqualTo(1);

        commentRepository.deleteAll();

    }

    @DisplayName("댓글이 존재할 경우, 대댓글이 작성됩니다.")
    @Test
    @Transactional
    void addReply() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);
        Post post = new Post();
        userRepository.save(user);
        postRepository.save(post);
        Long commentId = commentService.addComment(post.getId(), user.getUsername(), "hi there");

        //When
        commentService.addReply(commentId, "username", "reply1");
        commentService.addReply(commentId, "username", "reply2");

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertThat(commentRepository.count()).isEqualTo(3);
        Assertions.assertThat(commentRepository.findById(commentId+1L).get().getContent()).isEqualTo("reply1");

    }

    @DisplayName("유저네임과 commentId가 유효할 경우, 댓글이 수정된다.")
    @Test
    @Transactional
    void updateComment() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);
        Post post = new Post();

        userRepository.save(user);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), "username","original");

        //When
        commentService.updateCommentOrReply("username",commentId,"updated");

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertThat(commentRepository.findById(commentId).get().getContent()).isEqualTo("updated");

    }

    @DisplayName("유저가 삭제 될 경우 해당되는 댓글 역시 삭제 된다.")
    @Test
    @Transactional
    void commentDeletedWhenUserDeleted(){

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);
        Post post = new Post();

        userRepository.save(user);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), user.getUsername(), "comment");

        //When
        userRepository.delete(user);

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertThat(commentRepository.findAllByPost(postRepository.getById(post.getId())).size()).isEqualTo(0);
        Assertions.assertThat(commentRepository.findById(commentId).isEmpty()).isTrue();
    }

    @DisplayName("유저가 삭제 될 경우, 해당하는 대댓글 역시 삭제 된다.")
    @Test
    @Transactional
    void replyDeletedWhenUserDeleted(){

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);

        Post post = new Post();

        userRepository.save(user1);
        userRepository.save(user2);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), user1.getUsername(), "comment");
        Long replyId = commentService.addReply(commentId,user2.getUsername(),"reply");

        //When
        userRepository.delete(user2);

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertThat(commentRepository.findAllByPost(postRepository.getById(post.getId())).size()).isEqualTo(1);
        Assertions.assertThat(commentRepository.findById(replyId).isEmpty()).isTrue();
    }

    @DisplayName("제보가 삭제 될 경우, 해당하는 댓글과 대댓글 역시 삭제 된다.")
    @Test
    @Transactional
    void commentAndReplyDeletedWhenPostDeleted(){

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);

        Post post = new Post();

        userRepository.save(user1);
        userRepository.save(user2);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), user1.getUsername(), "comment");
        Long replyId = commentService.addReply(commentId,user2.getUsername(),"reply");

        //When
        postRepository.delete(post);

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertThat(commentRepository.findAll().size()).isEqualTo(0);
        Assertions.assertThat(commentRepository.findById(commentId).isEmpty()).isTrue();
        Assertions.assertThat(commentRepository.findById(replyId).isEmpty()).isTrue();

    }

    @DisplayName("유저 정보가 일치한 경우, 댓글이 지워진다.")
    @Test
    @Transactional
    void deleteCommentOrReply() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);
        Post post = new Post();

        userRepository.save(user);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), user.getUsername(), "comment");

        //When
        commentService.deleteCommentOrReply("username",commentId);

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertThat(commentRepository.findById(commentId).isEmpty()).isTrue();

    }

    @DisplayName("댓글을 지울 경우, 대댓글도 지워진다")
    @Test
    @Transactional
    void checkReplyDeletedWhenCommentDeleted() {

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);

        Post post = new Post();

        userRepository.save(user1);
        userRepository.save(user2);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), user1.getUsername(), "comment");
        Long replyId = commentService.addReply(commentId,user2.getUsername(),"reply");

        //When
        commentService.deleteCommentOrReply(user1.getUsername(),commentId);

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertThat(commentRepository.findById(commentId).isEmpty()).isTrue();
        Assertions.assertThat(commentRepository.findById(replyId).isEmpty()).isTrue();

    }

}