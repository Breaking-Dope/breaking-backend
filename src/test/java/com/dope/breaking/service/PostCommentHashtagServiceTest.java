package com.dope.breaking.service;

import com.dope.breaking.domain.hashtag.HashtagType;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.repository.PostCommentHashtagRepository;
import com.dope.breaking.repository.PostRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
class PostCommentHashtagServiceTest {

    @Autowired
    PostCommentHashtagService postCommentHashtagService;

    @Autowired
    PostCommentHashtagRepository postCommentHashtagRepository;

    @Autowired
    PostRepository postRepository;

    @Test
    void savePostHashtag() {
        //Given
        Post post = new Post();
        long postId = postRepository.save(post).getId();
        List<String> hashtag = new ArrayList<>();
        hashtag.add("hello1");
        hashtag.add("hello2");
        //When
        postCommentHashtagService.savePostHashtag(hashtag, postId, HashtagType.POST);

        //Then

        Assertions.assertSame(postCommentHashtagRepository.findAllByPost(post).size(), 2);
        Assertions.assertSame(postCommentHashtagRepository.findAllByPost(post).get(0).getHashtag().getHashtag(), "hello1");
        Assertions.assertSame(postCommentHashtagRepository.findAllByPost(post).get(1).getHashtag().getHashtag(), "hello2");
    }

    @Test
    void modifyPostHashtag() {
        //Given
        Post post = new Post();
        long postId = postRepository.save(post).getId();
        List<String> hashtag = new ArrayList<>();
        hashtag.add("hello1");
        hashtag.add("hello2");
        postCommentHashtagService.savePostHashtag(hashtag, postId, HashtagType.POST);

        //When
        hashtag.clear();
        hashtag.add("hello1");
        hashtag.add("hello3");
        postCommentHashtagService.modifyPostHashtag(hashtag, post, HashtagType.POST);

        //Then
        Assertions.assertSame(postCommentHashtagRepository.findAllByPost(post).size(), 2);
        Assertions.assertSame(postCommentHashtagRepository.findAllByPost(post).get(0).getHashtag().getHashtag(), "hello1");
        Assertions.assertSame(postCommentHashtagRepository.findAllByPost(post).get(1).getHashtag().getHashtag(), "hello3");

    }
}