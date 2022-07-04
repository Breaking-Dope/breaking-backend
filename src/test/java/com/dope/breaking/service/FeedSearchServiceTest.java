package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.repository.PostRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class FeedSearchServiceTest {
    @Autowired PostRepository postRepository;
    @Autowired SearchFeedService searchFeedService;
    @Autowired EntityManager em;

    @Test
    void get_10posts_without_filter() {
        for(int i = 0; i<25; i++) {
            Post post = new Post();
            postRepository.save(post);
        }

        Page<Post> paginationResult1 = searchFeedService.searchFeed(10,0);
        List<Post> content1 = paginationResult1.getContent();

        Page<Post> paginationResult2 = searchFeedService.searchFeed(10, 1);
        List<Post> content2 = paginationResult2.getContent();

        Page<Post> paginationResult3 = searchFeedService.searchFeed(10, 2);
        List<Post> content3 = paginationResult3.getContent();

        assertEquals(10, content1.size());
        assertEquals(10, content2.size());
        assertEquals(5, content3.size());
    }

    @Test
    void when_there_are_no_posts() {
        Page<Post> paginationResult = searchFeedService.searchFeed(10, 0);
        List<Post> content = paginationResult.getContent();

        assertEquals(0, content.size());
    }
}