package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.PostLikeRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
class PostLikeServiceTest {

    @Autowired private PostLikeService postLikeService;
    @Autowired private PostLikeRepository postLikeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
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
        Assertions.assertThat(postLikeRepository.existsPostLikesByUserAndPost(user,post1)).isTrue();
        Assertions.assertThat(postLikeRepository.existsPostLikesByUserAndPost(user,post2)).isTrue();
        Assertions.assertThat(postRepository.findById(post1.getId()).get().getPostLikeList().size()).isEqualTo(1);


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
        Assertions.assertThat(postLikeRepository.existsPostLikesByUserAndPost(user,post)).isFalse();
        Assertions.assertThat(postRepository.findById(post.getId()).get().getPostLikeList().size()).isEqualTo(0);

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
        Assertions.assertThat(postLikeRepository.countPostLikesByUser(user)).isEqualTo(0);
        Assertions.assertThat(postRepository.findById(post1.getId()).get().getPostLikeList().size()).isEqualTo(0);

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
        Assertions.assertThat(postLikeRepository.countPostLikesByPost(post)).isEqualTo(0);

    }

    @Test
    void likedUserList(){

        //Given
        User user1 = new User();
        User user2 = new User();
        Post post = new Post();

        userRepository.save(user1);
        userRepository.save(user2);
        postRepository.save(post);

        //When
        postLikeService.likePost(user1,post);
        postLikeService.likePost(user2,post);

        //Then
        Assertions.assertThat(postLikeService.likedUserList(null,post.getId()).size()).isEqualTo(2);
    }

}