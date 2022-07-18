package com.dope.breaking.service;

import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostType;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
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
    void create100DummyPosts(TestInfo info) {
        if (info.getDisplayName().equals("whenThereAreNoPosts()")) {
            return; // skip @BeforeEach in whenThereAreNoPosts test
        }

        for(int i = 0; i<30; i++) {
            Post post = Post.builder()
                    .title("title"+i)
                    .content("content"+i)
                    .postType(PostType.CHARGED)
                    .location(Location.builder().region("exampleRegion").latitude(i*100.0).longitude(i*100.0).build())
                    .price(i*1000)
                    .isHidden(false)
                    .isAnonymous(false)
                    .viewCount(i)
                    .build();
            postRepository.save(post);
        }

        for(int i = 0; i<30; i++) {
            Post post = Post.builder()
                    .title("title"+i)
                    .content("content"+i)
                    .postType(PostType.EXCLUSIVE)
                    .location(Location.builder().region("exampleRegion").latitude(i*100.0).longitude(i*100.0).build())
                    .price(i*1000)
                    .isHidden(false)
                    .isAnonymous(false)
                    .viewCount(i)
                    .build();
            postRepository.save(post);
        }

        for(int i = 0; i<30; i++) {
            Post post = Post.builder()
                    .title("title"+i)
                    .content("content"+i)
                    .postType(PostType.FREE)
                    .location(Location.builder().region("exampleRegion").latitude(i*100.0).longitude(i*100.0).build())
                    .price(i*1000)
                    .isHidden(false)
                    .isAnonymous(false)
                    .viewCount(i)
                    .build();
            postRepository.save(post);
        }

        //숨김 처리 된 포스트 10개
        for(int i = 0; i<10; i++) {
            Post post = Post.builder()
                    .title("title"+i)
                    .content("content"+i)
                    .postType(PostType.CHARGED)
                    .location(Location.builder().region("exampleRegion").latitude(i*100.0).longitude(i*100.0).build())
                    .price(i*1000)
                    .isHidden(true)
                    .isAnonymous(false)
                    .build();
            postRepository.save(post);
        }
    }

    @Test
    void whenThereAreNoPosts() {
        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .soldOption(SoldOption.SOLD)
                .build();
        Pageable pageable = PageRequest.of(0, 10);;

        Page<FeedResultPostDto> paginationResult = searchFeedService.searchFeed(searchFeedConditionDto, pageable);
        List<FeedResultPostDto> content = paginationResult.getContent();

        assertEquals(0, content.size());
    }

    @Test
    void get15postsWithoutFilter() {

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .soldOption(SoldOption.ALL)
                .build();
        Pageable pageable1 = PageRequest.of(0, 15);
        Pageable pageable2 = PageRequest.of(5, 15);
        Pageable lastPageable = PageRequest.of(6, 15);

        Page<FeedResultPostDto> paginationResult1 = searchFeedService.searchFeed(searchFeedConditionDto, pageable1);
        List<FeedResultPostDto> content1 = paginationResult1.getContent();

        Page<FeedResultPostDto> paginationResult2 = searchFeedService.searchFeed(searchFeedConditionDto, pageable2);
        List<FeedResultPostDto> content2 = paginationResult2.getContent();

        Page<FeedResultPostDto> lastPaginationResult = searchFeedService.searchFeed(searchFeedConditionDto, lastPageable);
        List<FeedResultPostDto> lastContent = lastPaginationResult.getContent();

        assertEquals(15, content1.size());
        assertEquals(15, content2.size());
        assertEquals(0, lastContent.size(), () -> "6번 페이지는, 0개가 나와야 한다.");
    }

}