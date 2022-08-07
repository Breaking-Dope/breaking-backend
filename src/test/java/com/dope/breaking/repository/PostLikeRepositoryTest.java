package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostLike;
import com.dope.breaking.domain.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PostLikeRepositoryTest {

    @Autowired UserRepository userRepository;
    @Autowired PostRepository postRepository;
    @Autowired PostLikeRepository postLikeRepository;

    @DisplayName("유저가 좋아요를 한 게시물이면, true가 반한된다.")
    @Test
    void postLikeExist() {

        User user = new User();
        userRepository.save(user);
        Post post = new Post();
        postRepository.save(post);
        PostLike postLike = new PostLike(user, post);
        postLikeRepository.save(postLike);

        assertTrue(postLikeRepository.existsByUserAndPostId(user, post.getId()));
    }
}
