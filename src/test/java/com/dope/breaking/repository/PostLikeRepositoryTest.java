package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostLike;
import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ForListInfoResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PostLikeRepositoryTest {

    @Autowired UserRepository userRepository;
    @Autowired PostRepository postRepository;
    @Autowired PostLikeRepository postLikeRepository;
    @Autowired FollowRepository followRepository;

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

    @DisplayName("유저가 좋아요 한 사람 리스트를 조회할 경우, 팔로우 여부가 정확히 반환 된다")
    @Test
    void postLikeList(){

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

        Assertions.assertFalse(postLikeRepository.postLikeList(user1,post,null,10).get(0).isFollowing());
        Assertions.assertTrue(postLikeRepository.postLikeList(user1,post,null,10).get(1).isFollowing());

    }

}
