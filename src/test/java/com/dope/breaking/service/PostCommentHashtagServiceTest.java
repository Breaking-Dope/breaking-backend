package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.PostHashtagRepository;
import com.dope.breaking.repository.PostRepository;
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
class PostHashtagServiceTest {

    @Autowired
    PostHashtagService postHashtagService;

    @Autowired
    PostHashtagRepository postHashtagRepository;

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
        postHashtagService.savePostHashtag(hashtag, postId);

        //Then

        Assertions.assertSame(postHashtagRepository.findAllByPost(post).size(), 2);
        Assertions.assertSame(postHashtagRepository.findAllByPost(post).get(0).getHashtag().getHashtag(), "hello1");
        Assertions.assertSame(postHashtagRepository.findAllByPost(post).get(1).getHashtag().getHashtag(), "hello2");
    }

    @Test
    void modifyPostHashtag() {
        //Given
        Post post = new Post();
        long postId = postRepository.save(post).getId();
        List<String> hashtag = new ArrayList<>();
        hashtag.add("hello1");
        hashtag.add("hello2");
        postHashtagService.savePostHashtag(hashtag, postId);

        //When
        hashtag.clear();
        hashtag.add("hello1");
        hashtag.add("hello3");
        postHashtagService.modifyPostHashtag(hashtag, post);

        //Then
        Assertions.assertSame(postHashtagRepository.findAllByPost(post).size(), 2);
        Assertions.assertSame(postHashtagRepository.findAllByPost(post).get(0).getHashtag().getHashtag(), "hello1");
        Assertions.assertSame(postHashtagRepository.findAllByPost(post).get(1).getHashtag().getHashtag(), "hello3");

    }
}