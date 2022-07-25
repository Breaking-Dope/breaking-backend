package com.dope.breaking.repository;

import com.dope.breaking.domain.hashtag.Hashtag;
import com.dope.breaking.domain.hashtag.PostCommentHashtag;
import com.dope.breaking.domain.post.Post;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PostCommentHashtagRepositoryTest {

    @Autowired
    PostCommentHashtagRepository postCommentHashtagRepository;

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
        PostCommentHashtag postCommentHashtag = PostCommentHashtag.builder()
                .post(post)
                .hashtag(hashtag).build();
        postCommentHashtagRepository.save(postCommentHashtag);

        //When
        boolean exist = postCommentHashtagRepository.existsByPostAndHashtag(post, hashtag);

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
        PostCommentHashtag postCommentHashtag1 = PostCommentHashtag.builder()
                .post(post)
                .hashtag(hashtag1).build();

        PostCommentHashtag postCommentHashtag2 = PostCommentHashtag.builder()
                .post(post)
                .hashtag(hashtag2).build();
        postCommentHashtagRepository.save(postCommentHashtag1);
        postCommentHashtagRepository.save(postCommentHashtag2);

        //When
        postCommentHashtagRepository.deleteAllByPost(post);

        //then
        Assertions.assertEquals(postCommentHashtagRepository.findAllByPost(post).size(), 0);
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
        PostCommentHashtag postCommentHashtag1 = PostCommentHashtag.builder()
                .post(post)
                .hashtag(hashtag1).build();

        PostCommentHashtag postCommentHashtag2 = PostCommentHashtag.builder()
                .post(post)
                .hashtag(hashtag2).build();
        postCommentHashtagRepository.save(postCommentHashtag1);
        postCommentHashtagRepository.save(postCommentHashtag2);

        //When
        int size = postCommentHashtagRepository.findAllByPost(post).size();

        //Then
        Assertions.assertEquals(size, 2);
    }
}