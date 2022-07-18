package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.PostLikeRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PostLikeServiceTest {

    @Autowired private PostLikeService postLikeService;
    @Autowired private PostLikeRepository postLikeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;


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

        //Then
        Assertions.assertThat(user.getPostLikeList().size()).isEqualTo(2);
        Assertions.assertThat(post1.getPostLikeList().size()).isEqualTo(1);
        Assertions.assertThat(post2.getPostLikeList().size()).isEqualTo(1);

        Assertions.assertThat(postLikeRepository.existsPostLikesByUserAndPost(user,post1)).isTrue();
        Assertions.assertThat(postLikeRepository.existsPostLikesByUserAndPost(user,post2)).isTrue();

    }

    @Test
    void unlikePost() {

        //Given
        User user = new User();
        Post post = new Post();

        userRepository.save(user);
        postRepository.save(post);

        postLikeService.likePost(user,post);

        //When
        postLikeService.unlikePost(user,post);

        //Then
        Assertions.assertThat(postLikeRepository.existsPostLikesByUserAndPost(user,post)).isFalse();

    }
    
}