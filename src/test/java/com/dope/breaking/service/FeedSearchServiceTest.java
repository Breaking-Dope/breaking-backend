package com.dope.breaking.service;

import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostType;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.FeedSearchConditionDto;
import com.dope.breaking.repository.FeedRepository;
import com.dope.breaking.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class FeedSearchServiceTest {
    @Autowired PostRepository postRepository;
    @Autowired FeedRepository feedRepository;
    @Autowired SearchFeedService searchFeedService;
    @Autowired EntityManager em;

    @BeforeEach
    void create25DummyPosts(TestInfo info) {
        if (info.getDisplayName().equals("whenThereAreNoPosts()")) {
            return; // skip @BeforeEach in whenThereAreNoPosts test
        }

        for(int i = 0; i<25; i++) {
            Post post = Post.builder()
                    .title("title"+i)
                    .content("content"+i)
                    .postType(PostType.CHARGED)
                    .location(Location.builder().region("exampleRegion").latitude(i*100.0).longitude(i*100.0).build())
                    .price(i*10000)
                    .isAnonymous(false)
                    .build();
            postRepository.save(post);
        }
    }

    @Test
    void get10postsWithoutFilter() {

        FeedSearchConditionDto feedSearchConditionDto = FeedSearchConditionDto.builder().build();
        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 10);
        Pageable pageable3 = PageRequest.of(2, 10);

        Page<FeedResultPostDto> paginationResult1 = searchFeedService.searchFeedPagination(feedSearchConditionDto, pageable1);
        List<FeedResultPostDto> content1 = paginationResult1.getContent();

        Page<FeedResultPostDto> paginationResult2 = searchFeedService.searchFeedPagination(feedSearchConditionDto, pageable2);
        List<FeedResultPostDto> content2 = paginationResult2.getContent();

        Page<FeedResultPostDto> paginationResult3 = searchFeedService.searchFeedPagination(feedSearchConditionDto, pageable3);
        List<FeedResultPostDto> content3 = paginationResult3.getContent();

        assertEquals(10, content1.size());
        assertEquals(10, content2.size());
        assertEquals(5, content3.size());
    }

    @Test
    void whenThereAreNoPosts() {
        FeedSearchConditionDto feedSearchConditionDto = FeedSearchConditionDto.builder().build();
        Pageable pageable = PageRequest.of(0, 10);;

        Page<FeedResultPostDto> paginationResult = searchFeedService.searchFeedPagination(feedSearchConditionDto, pageable);
        List<FeedResultPostDto> content = paginationResult.getContent();

        assertEquals(0, content.size());
    }
}