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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals(1, result.get(0).getLikeCount());
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

    @DisplayName("다른 사람이 숨긴 게시글은, 유저 페이지에 나타나지 않는다.")
    @Test
    void hideHiddenPostInOtherUserPage() {

        User owner = new User();
        userRepository.save(owner);
        Post post = Post.builder()
                .isHidden(true)
                .build();
        postRepository.save(post);
        Bookmark bookmark = new Bookmark(owner, post);
        bookmarkRepository.save(bookmark);

        em.flush();

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(1L)
                .soldOption(SoldOption.ALL)
                .build();

        List<FeedResultPostDto> result = feedRepository.searchUserPageBy(searchFeedConditionDto, owner, null, null);

        assertTrue(result.isEmpty());
    }

    @DisplayName("본인이 숨긴 게시글은, 본인 유저 페이지에서 나타난다.")
    @Test
    void displayHiddenPostInMyPage() {

        User user = new User();
        userRepository.save(user);
        Post post = Post.builder()
                .isHidden(true)
                .build();
        postRepository.save(post);
        Bookmark bookmark = new Bookmark(user, post);
        bookmarkRepository.save(bookmark);

        em.flush();

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(1L)
                .soldOption(SoldOption.ALL)
                .build();

        List<FeedResultPostDto> result = feedRepository.searchUserPageBy(searchFeedConditionDto, user, user, null);

        assertEquals(result.get(0).getPostId(), post.getId());
    }

    @DisplayName("익명 포스트는 다른 유저의 유저페이지에서 나타나지 않는다.")
    @Test
    void hideAnonymousPostInOtherUsersPage() {

        User user = new User();
        userRepository.save(user);
        Post post = Post.builder()
                .isAnonymous(true)
                .build();
        postRepository.save(post);

        em.flush();

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(1L)
                .soldOption(SoldOption.ALL)
                .build();

        List<FeedResultPostDto> result = feedRepository.searchUserPageBy(searchFeedConditionDto, user, null, null);

        assertEquals(0, result.size());
    }

    @DisplayName("익명 포스트는 나의 유저페이지에서 나타난다.")
    @Test
    void displayAnonymousPostInMyPage() {

        User user = new User();
        userRepository.save(user);
        Post post = Post.builder()
                .isAnonymous(true)
                .build();
        postRepository.save(post);

        em.flush();

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(1L)
                .soldOption(SoldOption.ALL)
                .build();

        List<FeedResultPostDto> result = feedRepository.searchUserPageBy(searchFeedConditionDto, user, user, null);

        assertEquals(1, result.size());
        assertEquals(post.getId(), result.get(0).getPostId());
    }

    @DisplayName("메인피드에서 만약 나의 포스트가 있으면, isMyPost 가 true로 반환된다.")
    @Test
    void setTrueIsMyPostWhenFindMyPost() {

        User user = new User();
        userRepository.save(user);
        Post post = Post.builder()
                .isAnonymous(true)
                .build();
        post.setUser(user);
        postRepository.save(post);

        em.flush();

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(1L)
                .soldOption(SoldOption.ALL)
                .build();

        List<FeedResultPostDto> result = feedRepository.searchFeedBy(searchFeedConditionDto, null, user);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsMyPost());
    }

    @DisplayName("문자열이 포함된 제목이나 본문을 가진 게시글을 검색한다")
    @Test
    void searchPostByString() {

        User user = new User();
        userRepository.save(user);

        Post post = Post.builder()
                .title("깻묵은 오늘도 맑음 뒤 흐림")
                .content("본문 없음")
                .build();
        post.setUser(user);
        postRepository.save(post);

        post = Post.builder()
                .title("제목이 없음")
                .content("안녕하세요 테스트 글입니다.")
                .build();
        post.setUser(user);
        postRepository.save(post);

        post = Post.builder()
                .title("제목이 없음")
                .content("본문 없음")
                .build();
        post.setUser(user);
        postRepository.save(post);

        em.flush();

        List<String> immutableList = List.of("one", "two", "three");

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(3L)
                .soldOption(SoldOption.ALL)
                .searchKeywordList(immutableList)
                .build();

        List<FeedResultPostDto> result = feedRepository.searchFeedBy(searchFeedConditionDto, null, user);
        assertEquals(2, result.size());
    }

    @DisplayName("띄어쓰기가 포함된 문자열은 그대로 검색된다.")
    @Test
    void searchPostByStringWithSpace() {

        User user = new User();
        userRepository.save(user);

        Post post = Post.builder()
                .title("깻묵은 오늘도 맑음 뒤 흐림")
                .content("본문 없음")
                .build();
        post.setUser(user);
        postRepository.save(post);

        post = Post.builder()
                .title("제목이 없음")
                .content("안녕하세요 테스트 글입니다.")
                .build();
        post.setUser(user);
        postRepository.save(post);

        post = Post.builder()
                .title("제목이 없음")
                .content("본문 없음")
                .build();
        post.setUser(user);
        postRepository.save(post);

        em.flush();

        List<String> immutableList = List.of("one", "two", "three")

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(3L)
                .soldOption(SoldOption.ALL)
                .searchKeywordList(immutableList)
                .build();

        List<FeedResultPostDto> result = feedRepository.searchFeedBy(searchFeedConditionDto, null, user);
        assertEquals(2, result.size());
    }

}
