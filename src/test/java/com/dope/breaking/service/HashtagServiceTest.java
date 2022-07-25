package com.dope.breaking.service;

import com.dope.breaking.domain.hashtag.Hashtag;
import com.dope.breaking.domain.hashtag.PostCommentHashtag;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.repository.HashtagRepository;
import com.dope.breaking.repository.PostCommentHashtagRepository;
import com.dope.breaking.repository.PostRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;


@SpringBootTest
@Transactional
class HashtagServiceTest {
    @Autowired
    PostRepository postRepository;

    @Autowired
    PostCommentHashtagRepository postCommentHashtagRepository;

    @Autowired
    HashtagRepository hashtagRepository;

    @Autowired
    HashtagService hashtagService;


    @Test
    public void existRelatedHashtag(){
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
        boolean exist = hashtagService.existRelatedHashtag(hashtag);

        //then
        Assertions.assertTrue(exist);

    }

    @Test
    public void deleteOrphanHashtag(){

        //Given
        Post post = new Post();
        long postId = postRepository.save(post).getId();
        Hashtag hashtag = Hashtag.builder()
                .hashtag("hello1").build();
        hashtagRepository.save(hashtag);
        Hashtag hashtag2 = Hashtag.builder()
                .hashtag("hello2").build();
        hashtagRepository.save(hashtag2);
        PostCommentHashtag postCommentHashtag = PostCommentHashtag.builder()
                .post(post)
                .hashtag(hashtag).build();
        postCommentHashtagRepository.save(postCommentHashtag);

        List<String> hashtags = new LinkedList<>();
        hashtags.add("hello1");
        hashtags.add("hello2");

        //When
        hashtagService.deleteOrphanHashtag(hashtags);
        boolean exist = hashtagRepository.existsByHashtag("hello2");

        //then
        Assertions.assertFalse(exist);

    }

}