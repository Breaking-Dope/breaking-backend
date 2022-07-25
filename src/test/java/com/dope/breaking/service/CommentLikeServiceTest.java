package com.dope.breaking.service;

import com.dope.breaking.domain.comment.CommentLike;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.exception.like.AlreadyLikedException;
import com.dope.breaking.exception.like.AlreadyUnlikedException;
import com.dope.breaking.repository.CommentLikeRepository;
import com.dope.breaking.repository.CommentRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentLikeServiceTest {

    @Autowired
    private CommentLikeService commentLikeService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("유저가 좋아하지 않은 댓글을 좋아요를 시도할 경우, 좋아요가 정상 실행 된다.")
    @Test
    @Transactional
    void likeComment() {

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);

        Post post = new Post();

        userRepository.save(user1);
        userRepository.save(user2);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(),user1.getUsername(),"hello");

        //When
        commentLikeService.likeComment(user2.getUsername(),commentId);

        //Then
        Assertions.assertTrue(commentLikeRepository.existsCommentLikeByUserAndComment(user2,commentRepository.findById(commentId).get()));

    }

    @DisplayName("유저가 이미 댓글을 좋아할 때 좋아요를 시도할 경우, 예외가 발생한다.")
    @Transactional
    @Test
    void likeAlreadyLikedComment() {

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);

        Post post = new Post();

        userRepository.save(user1);
        userRepository.save(user2);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(),user1.getUsername(),"hello");
        CommentLike commentLike = new CommentLike(user2,commentRepository.findById(commentId).get());
        commentLikeRepository.save(commentLike);

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertThrows(AlreadyLikedException.class, ()
                ->  commentLikeService.likeComment(user2.getUsername(),commentId)); //When

    }

    @DisplayName("유저가 이미 좋아한 댓글을 좋아요 취소할 경우, 좋아요가 정상적으로 취소된다.")
    @Transactional
    @Test
    void unlikeComment() {

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);

        Post post = new Post();

        userRepository.save(user1);
        userRepository.save(user2);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(),user1.getUsername(),"hello");
        CommentLike commentLike = new CommentLike(user2,commentRepository.findById(commentId).get());
        commentLikeRepository.save(commentLike);

        entityManager.flush();
        entityManager.clear();

        //When
        commentLikeService.unlikeComment(user2.getUsername(),commentId);

        //Then
        Assertions.assertFalse(commentLikeRepository.existsCommentLikeByUserAndComment(user2,commentRepository.findById(commentId).get()));

    }

    @DisplayName("유저가 이미 좋아하지 않는 댓글을 좋아요 취소할 경우, 예외가 발생한다.")
    @Transactional
    @Test
    void unlikeAlreadyUnlikedComment() {

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);

        Post post = new Post();

        userRepository.save(user1);
        userRepository.save(user2);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(),user1.getUsername(),"hello");

        Assertions.assertThrows(AlreadyUnlikedException.class, ()
                ->  commentLikeService.unlikeComment(user2.getUsername(),commentId)); //When

    }

    @DisplayName("유저가 탈퇴할 경우, 해당되는 댓글/대댓글 좋아요도 삭제된다.")
    @Transactional
    @Test
    void commentAndReplyLikeDeletedWhenUserDeleted() {

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);

        Post post =  new Post();

        userRepository.save(user1);
        userRepository.save(user2);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(),"username1","comment");
        Long replyId = commentService.addReply(commentId,"username1","reply");

        commentLikeService.likeComment("username2", commentId);
        commentLikeService.likeComment("username2", replyId);

        //When
        userRepository.deleteById(user2.getId());
        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertEquals(0,commentRepository.getById(commentId).getCommentLikeList().size());
        Assertions.assertEquals(0,commentRepository.getById(replyId).getCommentLikeList().size());

    }

    @DisplayName("댓글이 삭제 된 경우, 해당되는 댓글/대댓글 좋아요도 삭제된다.")
    @Transactional
    @Test
    void replyLikesDeletedWhenCommentDeleted() {

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);

        Post post =  new Post();

        userRepository.save(user1);
        userRepository.save(user2);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(),"username1","comment");
        Long replyId = commentService.addReply(commentId,"username1","reply");

        commentLikeService.likeComment("username2", commentId);
        commentLikeService.likeComment("username2", replyId);

        //When
        commentRepository.deleteById(commentId);

        //Then
        Assertions.assertEquals(0,commentLikeRepository.findAll().size());

    }


    @DisplayName("대댓글이 삭제될 경우, 해당되는 대댓글 좋아요도 삭제된다.")
    @Transactional
    @Test
    void replyLikesDeletedWhenReplyDeleted() {

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);

        Post post =  new Post();

        userRepository.save(user1);
        userRepository.save(user2);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(),"username1","comment");
        Long replyId = commentService.addReply(commentId,"username1","reply");

        commentLikeService.likeComment("username2", commentId);
        commentLikeService.likeComment("username2", replyId);

        //When
        commentRepository.deleteById(replyId);

        //Then
        Assertions.assertEquals(1,commentLikeRepository.findAll().size());

    }


}