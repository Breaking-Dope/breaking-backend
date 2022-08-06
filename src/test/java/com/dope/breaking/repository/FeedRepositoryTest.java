package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostLike;
import com.dope.breaking.domain.user.Bookmark;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.service.SearchFeedService;
import com.dope.breaking.service.SoldOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class FeedRepositoryTest {

    @Autowired FeedRepository feedRepository;
    @Autowired UserRepository userRepository;
    @Autowired PostRepository postRepository;
    @Autowired PostLikeRepository postLikeRepository;
    @Autowired BookmarkRepository bookmarkRepository;

    @Autowired EntityManager em;

    @DisplayName("유저가 like한 게시글은, isLiked가 true로 반환된다.")
    @Test
    void feedIsLike() {

        User user = new User();
        userRepository.save(user);
        Post post = new Post();
        postRepository.save(post);
        PostLike postLike = new PostLike(user, post);
        postLikeRepository.save(postLike);

        em.flush();

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(1L)
                .soldOption(SoldOption.ALL)
                .build();

        List<FeedResultPostDto> result = feedRepository.searchFeedBy(searchFeedConditionDto, null, user);

        assertTrue(result.get(0).getIsLiked());
    }

    @DisplayName("유저가 bookmark한 게시글은, isBookmarked가 true로 반환된다.")
    @Test
    void feedIsBookmarked() {

        User user = new User();
        userRepository.save(user);
        Post post = new Post();
        postRepository.save(post);
        Bookmark bookmark = new Bookmark(user, post);
        bookmarkRepository.save(bookmark);

        em.flush();

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(1L)
                .soldOption(SoldOption.ALL)
                .build();

        List<FeedResultPostDto> result = feedRepository.searchFeedBy(searchFeedConditionDto, null, user);

        assertTrue(result.get(0).getIsBookmarked());
    }

}
