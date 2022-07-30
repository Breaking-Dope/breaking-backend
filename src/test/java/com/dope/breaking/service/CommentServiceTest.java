package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.comment.SearchCommentConditionDto;
import com.dope.breaking.exception.comment.NoSuchCommentException;
import com.dope.breaking.exception.post.NoSuchPostException;
import com.dope.breaking.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
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
    @Autowired
    private HashtagRepository hashtagRepository;

    @DisplayName("제보가 존재할 경우, 댓글이 작성된다.")
    @Test
    void addComment() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);
        Post post = new Post();

        userRepository.save(user);
        postRepository.save(post);

        //When
        commentService.addComment(post.getId(), user.getUsername(), "comment",null);

        entityManager.flush();
        entityManager.clear();

        //Then
        assertEquals(1, commentRepository.count());

        commentRepository.deleteAll();

    }

    @DisplayName("댓글이 존재할 경우, 대댓글이 작성된다.")
    @Test
    void addReply() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);
        Post post = new Post();
        userRepository.save(user);
        postRepository.save(post);
        Long commentId = commentService.addComment(post.getId(), user.getUsername(), "hi there",null);

        //When
        commentService.addReply(commentId, "username", "reply1",null);
        commentService.addReply(commentId, "username", "reply2",null);

        entityManager.flush();
        entityManager.clear();

        //Then
        assertEquals(3, commentRepository.count());
        assertEquals("reply1", commentRepository.findById(commentId+1L).get().getContent());

    }

    @DisplayName("유저네임과 commentId가 유효할 경우, 댓글이 수정된다.")
    @Test
    void updateComment() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);
        Post post = new Post();

        userRepository.save(user);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), "username","original",null);

        //When
        commentService.updateCommentOrReply("username",commentId,"updated",null);

        entityManager.flush();
        entityManager.clear();

        //Then
        assertEquals("updated", commentRepository.findById(commentId).get().getContent());

    }

    @DisplayName("유저가 삭제 될 경우 해당되는 댓글 역시 삭제 된다.")
    @Test
    void commentDeletedWhenUserDeleted(){

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);
        Post post = new Post();

        userRepository.save(user);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), user.getUsername(), "comment",null);

        //When
        userRepository.delete(user);

        entityManager.flush();
        entityManager.clear();

        //Then
        assertEquals(0, commentRepository.findAllByPost(postRepository.getById(post.getId())).size());
        assertTrue(commentRepository.findById(commentId).isEmpty());
    }

    @DisplayName("유저가 삭제 될 경우, 해당하는 대댓글 역시 삭제 된다.")
    @Test
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

        Long commentId = commentService.addComment(post.getId(), user1.getUsername(), "comment",null);
        Long replyId = commentService.addReply(commentId,user2.getUsername(),"reply",null);

        //When
        userRepository.delete(user2);

        entityManager.flush();
        entityManager.clear();

        //Then
        assertEquals(1, commentRepository.findAllByPost(postRepository.getById(post.getId())).size());
        assertTrue(commentRepository.findById(replyId).isEmpty());
    }

    @DisplayName("제보가 삭제 될 경우, 해당하는 댓글과 대댓글 역시 삭제 된다.")
    @Test
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

        Long commentId = commentService.addComment(post.getId(), user1.getUsername(), "comment",null);
        Long replyId = commentService.addReply(commentId,user2.getUsername(),"reply",null);

        //When
        postRepository.delete(post);

        entityManager.flush();
        entityManager.clear();

        //Then
        assertEquals(0, commentRepository.findAll().size());
        assertTrue(commentRepository.findById(commentId).isEmpty());
        assertTrue(commentRepository.findById(replyId).isEmpty());

    }

    @DisplayName("유저 정보가 일치한 경우, 댓글이 지워진다.")
    @Test
    void deleteCommentOrReply() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);
        Post post = new Post();

        userRepository.save(user);
        postRepository.save(post);

        Long commentId = commentService.addComment(post.getId(), user.getUsername(), "comment",null);

        //When
        commentService.deleteCommentOrReply("username",commentId);

        entityManager.flush();
        entityManager.clear();

        //Then
        assertTrue(commentRepository.findById(commentId).isEmpty());

    }

    @DisplayName("댓글을 지울 경우, 대댓글도 지워진다")
    @Test
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

        Long commentId = commentService.addComment(post.getId(), user1.getUsername(), "comment",null);
        Long replyId = commentService.addReply(commentId,user2.getUsername(),"reply",null);

        //When
        commentService.deleteCommentOrReply(user1.getUsername(),commentId);

        entityManager.flush();
        entityManager.clear();

        //Then
        assertTrue(commentRepository.findById(commentId).isEmpty());
        assertTrue(commentRepository.findById(replyId).isEmpty());

    }

    @DisplayName("댓글에 해시태그가 존재할 경우, 해시태그가 정상적으로 등록된다")
    @Test
    void checkHashtagsAdded(){

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);

        Post post = new Post();

        userRepository.save(user1);
        userRepository.save(user2);
        postRepository.save(post);

        //When
        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("hashtag1");
        hashtagList.add("hashtag2");
        hashtagList.add("hashtag2");

        Long commentId = commentService.addComment(post.getId(), user1.getUsername(), "comment",null);
        Long replyId = commentService.addReply(commentId,user2.getUsername(),"reply",hashtagList);

        entityManager.flush();
        entityManager.clear();

        //Then
        assertEquals(3, hashtagRepository.findAll().size());

    }

    @DisplayName("댓글을 삭제할 경우, 해당하는 Hashtag 객체도 삭제된다.")
    @Test
    void checkNoHashtagWhenNullList(){

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username2", Role.USER);

        Post post = new Post();

        userRepository.save(user1);
        userRepository.save(user2);
        postRepository.save(post);

        //When
        List<String> hashtagList = new ArrayList<>();
        hashtagList.add("hashtag1");
        hashtagList.add("hashtag2");
        hashtagList.add("hashtag2");
        hashtagList.add("hashtag2");

        Long commentId = commentService.addComment(post.getId(), user1.getUsername(), "comment",null);
        Long replyId = commentService.addReply(commentId,user2.getUsername(),"reply",hashtagList);

        entityManager.flush();
        entityManager.clear();

        commentService.deleteCommentOrReply(user2.getUsername(), replyId);

        //Then
        assertEquals(0, hashtagRepository.findAll().size());
        assertEquals(1, commentRepository.findAll().size());

    }

    @DisplayName("존재하지 않은 post id가 입력되면, 예외가 발생한다.")
    @Test
    void getCommentListFailure() {
        User user = User.builder()
            .username("12345g")
            .build();
        userRepository.save(user);

        entityManager.flush();
        entityManager.clear();

        SearchCommentConditionDto searchCommentConditionDto = SearchCommentConditionDto.builder()
                .targetType(CommentTargetType.POST)
                .targetId(999L)
                .build();
        assertThrows(NoSuchPostException.class,
                () -> commentService.getCommentList(searchCommentConditionDto, "12345g"));

    }

    @DisplayName("존재하지 않은 parent comment id가 입력되면, 예외가 발생한다.")
    @Test
    void getReplyCommentListFailure() {
        User user = User.builder()
                .username("12345g")
                .build();
        userRepository.save(user);

        entityManager.flush();
        entityManager.clear();

        SearchCommentConditionDto searchCommentConditionDto = SearchCommentConditionDto.builder()
                .targetType(CommentTargetType.COMMENT)
                .targetId(999L)
                .build();
        assertThrows(NoSuchCommentException.class,
                () -> commentService.getCommentList(searchCommentConditionDto, "12345g"));

    }

}