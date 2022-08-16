package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostLike;
import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.exception.pagination.InvalidCursorException;
import com.dope.breaking.repository.FollowRepository;
import com.dope.breaking.repository.PostLikeRepository;
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
@Transactional
class PostLikeServiceTest {

    @Autowired private PostLikeService postLikeService;
    @Autowired private PostLikeRepository postLikeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private FollowRepository followRepository;
    @Autowired private EntityManager entityManager;

    @Test
    void likePost() {

        //Given
        User user = new User();
        Post post1 = new Post();
        Post post2 = new Post();

        userRepository.save(user);
        postRepository.save(post1);
        postRepository.save(post2);

        //When
        postLikeService.likePost(user,post1);
        postLikeService.likePost(user,post2);

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertTrue(postLikeRepository.existsPostLikesByUserAndPost(user,post1));
        Assertions.assertTrue(postLikeRepository.existsPostLikesByUserAndPost(user,post2));
        Assertions.assertEquals(1,postRepository.findById(post1.getId()).get().getPostLikeList().size());

    }

    @Test
    void unlikePost() {

        //Given
        User user = new User();
        Post post = new Post();

        userRepository.save(user);
        postRepository.save(post);

        postLikeService.likePost(user,post);

        entityManager.flush();
        entityManager.clear();

        //When
        postLikeService.unlikePost(user,post);

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertFalse(postLikeRepository.existsPostLikesByUserAndPost(user,post));
        Assertions.assertEquals(0,postRepository.findById(post.getId()).get().getPostLikeList().size());

    }

    @Test
    void checkPostLikeDeleteWhenUserDeleted(){

        //Given
        User user = new User();
        Post post1 = new Post();
        Post post2 = new Post();

        userRepository.save(user);
        postRepository.save(post1);
        postRepository.save(post2);

        postLikeService.likePost(user,post1);
        postLikeService.likePost(user,post2);

        //When
        userRepository.delete(user);

        entityManager.flush();
        entityManager.clear();

        //Then
        Assertions.assertEquals(0,postLikeRepository.countPostLikesByUser(user));
        Assertions.assertEquals(0,postRepository.findById(post1.getId()).get().getPostLikeList().size());

    }

    @Test
    void checkPostLikeDeleteWhenPostDeleted(){

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
        postRepository.delete(post);
        entityManager.flush();

        //Then
        Assertions.assertEquals(0,postLikeRepository.countPostLikesByPost(post));

    }


    @DisplayName("cursorId가 유효하지 않은 경우, 예외가 발생한다.")
    @Test
    void purchaseListWithInvalidCursorId(){

        //Given
        User user1 = new User();
        user1.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "username2", Role.USER);
        userRepository.save(user1);
        userRepository.save(user2);

        Post post = new Post();
        postRepository.save(post);
        Post post2 = new Post();
        postRepository.save(post2);

        PostLike postLike1 = new PostLike(user1, post);
        PostLike postLike2 = new PostLike(user2, post);
        PostLike postLike3 = new PostLike(user2, post2);
        postLikeRepository.save(postLike1);
        postLikeRepository.save(postLike3);
        postLikeRepository.save(postLike2);

        Follow follow = new Follow(user1,user2);
        followRepository.save(follow);

        //Then
        Assertions.assertThrows(InvalidCursorException.class,
                ()->postLikeService.postLikeList(user1.getUsername(),post.getId(),100L,10)); //When

        Assertions.assertThrows(InvalidCursorException.class,
                ()->postLikeService.postLikeList(user1.getUsername(),post.getId(),2L,10)); //When

    }

    @DisplayName("cursorId와 유저네임이 유효한 경우, 좋아요한 사람의 리스트가 정확히 반환된다.")
    @Test
    void purchaseListWithValidCursorIdAndUsername(){

        //Given
        User user1 = new User();
        user1.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "username1", Role.USER);
        User user2 = new User();
        user2.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "username2", Role.USER);
        userRepository.save(user1);
        userRepository.save(user2);

        Post post = new Post();
        postRepository.save(post);

        PostLike postLike1 = new PostLike(user1, post);
        PostLike postLike2 = new PostLike(user2, post);
        postLikeRepository.save(postLike1);
        postLikeRepository.save(postLike2);

        Follow follow = new Follow(user1,user2);
        followRepository.save(follow);

        //Then                                       //When
        org.junit.jupiter.api.Assertions.assertFalse(postLikeService.postLikeList(user1.getUsername(),post.getId(),null,10).get(0).isFollowing());
        org.junit.jupiter.api.Assertions.assertTrue(postLikeService.postLikeList(user1.getUsername(),post.getId(),null,10).get(1).isFollowing());

    }

}