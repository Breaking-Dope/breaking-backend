package com.dope.breaking.repository;

import com.dope.breaking.domain.hashtag.Hashtag;
import com.dope.breaking.domain.hashtag.PostHashtag;
import com.dope.breaking.domain.post.Post;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PostHashtagRepositoryTest {

    @Autowired
    PostHashtagRepository postHashtagRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    HashtagRepository hashtagRepository;


    @Test
    void existsByPostAndHashtag() {
        //Given
        Post post = new Post();
        long postId = postRepository.save(post).getId();
        Hashtag hashtag = Hashtag.builder()
                .hashtag("hello1").build();
        hashtagRepository.save(hashtag);
        PostHashtag postHashtag = PostHashtag.builder()
                .post(post)
                .hashtag(hashtag).build();
        postHashtagRepository.save(postHashtag);

        //When
        boolean exist = postHashtagRepository.existsByPostAndHashtag(post, hashtag);

        //then
        Assertions.assertTrue(exist);
    }

    @Test
    void deleteAllByPost() {
        //Given
        Post post = new Post();
        long postId = postRepository.save(post).getId();
        Hashtag hashtag1 = Hashtag.builder()
                .hashtag("hello1").build();
        hashtagRepository.save(hashtag1);
        Hashtag hashtag2 = Hashtag.builder()
                .hashtag("hello2").build();
        hashtagRepository.save(hashtag2);
        PostHashtag postHashtag1 = PostHashtag.builder()
                .post(post)
                .hashtag(hashtag1).build();

        PostHashtag postHashtag2 = PostHashtag.builder()
                .post(post)
                .hashtag(hashtag2).build();
        postHashtagRepository.save(postHashtag1);
        postHashtagRepository.save(postHashtag2);

        //When
        postHashtagRepository.deleteAllByPost(post);

        //then
        Assertions.assertEquals(postHashtagRepository.findAllByPost(post).size(), 0);
    }

    @Test
    void findAllByPost() {
        //Given
        Post post = new Post();
        long postId = postRepository.save(post).getId();
        Hashtag hashtag1 = Hashtag.builder()
                .hashtag("hello1").build();
        hashtagRepository.save(hashtag1);
        Hashtag hashtag2 = Hashtag.builder()
                .hashtag("hello2").build();
        hashtagRepository.save(hashtag2);
        PostHashtag postHashtag1 = PostHashtag.builder()
                .post(post)
                .hashtag(hashtag1).build();

        PostHashtag postHashtag2 = PostHashtag.builder()
                .post(post)
                .hashtag(hashtag2).build();
        postHashtagRepository.save(postHashtag1);
        postHashtagRepository.save(postHashtag2);

        //When
        int size = postHashtagRepository.findAllByPost(post).size();

        //Then
        Assertions.assertEquals(size, 2);
    }
}