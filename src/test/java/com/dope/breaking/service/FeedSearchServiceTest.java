package com.dope.breaking.service;

import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostLike;
import com.dope.breaking.domain.post.PostType;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.repository.FeedRepository;
import com.dope.breaking.repository.PostLikeRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class FeedSearchServiceTest {

    @Autowired PostRepository postRepository;
    @Autowired FeedRepository feedRepository;
    @Autowired PostLikeRepository postLikeRepository;
    @Autowired SearchFeedService searchFeedService;
    @Autowired EntityManager em;
    @Autowired UserRepository userRepository;

    @DisplayName("포스트가 없으면, 에러가 나지 않고 빈 배열을 반환한다.")
    @Test
    void whenThereAreNoPosts() {
        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .cursorId(null)
                .size(15L)
                .soldOption(SoldOption.SOLD)
                .build();

        List<FeedResultPostDto> result = searchFeedService.searchFeed(searchFeedConditionDto);

        assertEquals(0, result.size());
    }

    @DisplayName("일반 포스트 90개와, 숨김 처리된 포스트 10개를 생성하고, 15개씩 조회한다.")
    @Test
    void get15postsWithoutFilterFrom100Dummy() {

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
                    .title("title"+(i+30))
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
                    .title("title"+(i+60))
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

        Long page = 15L;

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .cursorId(null)
                .size(page)
                .soldOption(SoldOption.ALL)
                .sortStrategy(SortStrategy.CHRONOLOGICAL)
                .build();

        List<FeedResultPostDto> content1 = searchFeedService.searchFeed(searchFeedConditionDto);
        searchFeedConditionDto.setCursorId(content1.get(content1.size() - 1).getPostId());
        List<FeedResultPostDto> content2 = searchFeedService.searchFeed(searchFeedConditionDto);
        searchFeedConditionDto.setCursorId(content2.get(content2.size() - 1).getPostId());
        List<FeedResultPostDto> content3 = searchFeedService.searchFeed(searchFeedConditionDto);
        searchFeedConditionDto.setCursorId(content3.get(content3.size() - 1).getPostId());
        List<FeedResultPostDto> content4 = searchFeedService.searchFeed(searchFeedConditionDto);
        searchFeedConditionDto.setCursorId(content4.get(content4.size() - 1).getPostId());
        List<FeedResultPostDto> content5 = searchFeedService.searchFeed(searchFeedConditionDto);
        searchFeedConditionDto.setCursorId(content5.get(content5.size() - 1).getPostId());
        List<FeedResultPostDto> content6 = searchFeedService.searchFeed(searchFeedConditionDto);
        searchFeedConditionDto.setCursorId(content6.get(content6.size() - 1).getPostId());
        List<FeedResultPostDto> content7 = searchFeedService.searchFeed(searchFeedConditionDto);

        assertEquals(page, content1.size());
        assertEquals(page, content2.size());
        assertEquals(page, content6.size());
        assertEquals(0, content7.size(), () -> "남은 포스트 10개는 숨김처리되어 조회되지 않는다.");
    }

}