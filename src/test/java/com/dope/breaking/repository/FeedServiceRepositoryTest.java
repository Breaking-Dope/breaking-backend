package com.dope.breaking.repository;

import com.dope.breaking.domain.financial.Purchase;
import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostLike;
import com.dope.breaking.domain.post.PostType;
import com.dope.breaking.domain.user.Bookmark;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.service.SoldOption;
import com.dope.breaking.service.SortStrategy;
import com.dope.breaking.service.UserPageFeedOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static com.dope.breaking.domain.user.Role.PRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class FeedServiceRepositoryTest {

    @Autowired PostRepository postRepository;

    @Autowired FeedRepository feedRepository;
    @Autowired PostLikeRepository postLikeRepository;
    @Autowired
    EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired BookmarkRepository bookmarkRepository;
    @Autowired PurchaseRepository purchaseRepository;

    @DisplayName("포스트가 없으면, 에러가 나지 않고 빈 배열을 반환한다.")
    @Test
    void whenThereAreNoPosts() {
        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(15L)
                .soldOption(SoldOption.ALL)
                .build();

        List<FeedResultPostDto> result = feedRepository.searchFeedBy(searchFeedConditionDto, null, null);

        assertEquals(0, result.size());
    }


    @DisplayName("좋아요 정렬 옵션은 좋아요가 많은 포스트를 먼저 조회한다.")
    @Test
    void likeSortStrategy() {
        User user = new User();
        userRepository.save(user);

        //좋아요 받은 포스트 생성
        Post firstPost = Post.builder()
                .postType(PostType.CHARGED)
                .isHidden(false)
                .isAnonymous(false)
                .build();
        postRepository.save(firstPost);
        postLikeRepository.save(new PostLike(user, firstPost));

        //좋아요 없는 포스트 생성
        Post secondPost = Post.builder()
                .postType(PostType.CHARGED)
                .isHidden(false)
                .isAnonymous(false)
                .build();
        postRepository.save(secondPost);

        //좋아요 없는 포스트 생성
        Post thirdPost = Post.builder()
                .postType(PostType.CHARGED)
                .isHidden(false)
                .isAnonymous(false)
                .build();
        postRepository.save(thirdPost);
        postLikeRepository.save(new PostLike(user, thirdPost));

        em.flush();
        em.clear();

        //첫번째 게시글 조회
        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(1L)
                .soldOption(SoldOption.ALL)
                .sortStrategy(SortStrategy.LIKE)
                .build();
        List<FeedResultPostDto> content1 = feedRepository.searchFeedBy(searchFeedConditionDto, null, null);

        //두번째 게시글 조회
        Post cursorPost = postRepository.findById(content1.get(0).getPostId()).get();
        List<FeedResultPostDto> content2 = feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost, null);

        //세번째 게시글 조회
        cursorPost = postRepository.findById(content2.get(0).getPostId()).get();
        List<FeedResultPostDto> content3 = feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost, null);

        //then 3 1 2 순서
        assertEquals(thirdPost.getId(), content1.get(0).getPostId(), ()->"좋아요가 1인 게시글이 조회된다.");
        assertEquals(firstPost.getId(), content2.get(0).getPostId(), ()->"좋아요가 1인 게시글이 조회된다.");
        assertEquals(secondPost.getId(), content3.get(0).getPostId(), ()->"좋아요가 0인 게시글이 조회된다.");

    }

    @DisplayName("좋아요 정렬 옵션은 좋아요가 많은 포스트를 먼저 조회한다.")
    @Test
    void viewSortStrategy() {
        User user = new User();
        userRepository.save(user);

        Post firstPost = Post.builder()
                .postType(PostType.CHARGED)
                .isHidden(false)
                .isAnonymous(false)
                .viewCount(2)
                .build();
        postRepository.save(firstPost);

        Post secondPost = Post.builder()
                .postType(PostType.CHARGED)
                .isHidden(false)
                .isAnonymous(false)
                .viewCount(3)
                .build();
        postRepository.save(secondPost);

        Post thirdPost = Post.builder()
                .postType(PostType.CHARGED)
                .isHidden(false)
                .isAnonymous(false)
                .viewCount(1)
                .build();
        postRepository.save(thirdPost);

        Post fourthPost = Post.builder()
                .postType(PostType.CHARGED)
                .isHidden(false)
                .isAnonymous(false)
                .viewCount(2)
                .build();
        postRepository.save(fourthPost);

        em.flush();
        em.clear();

        //첫번째 게시글 조회
        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(1L)
                .soldOption(SoldOption.ALL)
                .sortStrategy(SortStrategy.VIEW)
                .build();
        List<FeedResultPostDto> content1 = feedRepository.searchFeedBy(searchFeedConditionDto, null, null);

        //두번째 게시글 조회
        Post cursorPost = postRepository.findById(content1.get(0).getPostId()).get();
        List<FeedResultPostDto> content2 = feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost, null);

        //세번째 게시글 조회
        cursorPost = postRepository.findById(content2.get(0).getPostId()).get();
        List<FeedResultPostDto> content3 = feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost, null);

        //네번째 게시글 조회
        cursorPost = postRepository.findById(content3.get(0).getPostId()).get();
        List<FeedResultPostDto> content4 = feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost, null);

        //then 3 1 2 순서
        assertEquals(secondPost.getId(), content1.get(0).getPostId(), ()->content1.get(0).getViewCount() + " :조회수가 3인 게시글이 조회된다.");
        assertEquals(fourthPost.getId(), content2.get(0).getPostId(), ()->content2.get(0).getViewCount() + " :조회수가 2인 게시글이 조회된다. id가 높은게 우선 조회된다.");
        assertEquals(firstPost.getId(), content3.get(0).getPostId(), ()-> content3.get(0).getViewCount() + " :조회수가 2인 게시글이 조회된다.");
        assertEquals(thirdPost.getId(), content4.get(0).getPostId(), ()->content4.get(0).getViewCount() + " :조회수가 0인 게시글이 조회된다.");

    }

    @DisplayName("일반 포스트 90개와, 숨김 처리된 포스트 10개를 생성하고, 15개씩 조회한다.")
    @Test
    void get15postsWithoutFilterFrom100Dummy() {

        for(int i = 0; i<30; i++) {
            Post post = Post.builder()
                    .title("title"+i)
                    .content("content"+i)
                    .postType(PostType.CHARGED)
                    .location(Location.builder().address("exampleAddress").latitude(i*100.0).longitude(i*100.0).region_1depth_name("region1").region_2depth_name("region2").build())
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
                    .location(Location.builder().address("exampleAddress").latitude(i*100.0).longitude(i*100.0).region_1depth_name("region1").region_2depth_name("region2").build())
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
                    .location(Location.builder().address("exampleAddress").latitude(i*100.0).longitude(i*100.0).region_1depth_name("region1").region_2depth_name("region2").build())
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
                    .location(Location.builder().address("exampleAddress").latitude(i*100.0).longitude(i*100.0).region_1depth_name("region1").region_2depth_name("region2").build())
                    .price(i*1000)
                    .isHidden(true)
                    .isAnonymous(false)
                    .build();
            postRepository.save(post);
        }

        Long page = 15L;

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(page)
                .soldOption(SoldOption.ALL)
                .sortStrategy(SortStrategy.CHRONOLOGICAL)
                .build();

        List<FeedResultPostDto> content1 = feedRepository.searchFeedBy(searchFeedConditionDto, null, null);
        Post cursorPost = postRepository.findById(content1.get(content1.size() - 1).getPostId()).get();
        List<FeedResultPostDto> content2 = feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost, null);
        cursorPost = postRepository.findById(content2.get(content1.size() - 1).getPostId()).get();
        List<FeedResultPostDto> content3 = feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost, null);
        cursorPost = postRepository.findById(content3.get(content1.size() - 1).getPostId()).get();
        List<FeedResultPostDto> content4 = feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost, null);
        cursorPost = postRepository.findById(content4.get(content1.size() - 1).getPostId()).get();
        List<FeedResultPostDto> content5 = feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost, null);
        cursorPost = postRepository.findById(content5.get(content1.size() - 1).getPostId()).get();
        List<FeedResultPostDto> content6 = feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost, null);
        cursorPost = postRepository.findById(content6.get(content1.size() - 1).getPostId()).get();
        List<FeedResultPostDto> content7 = feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost, null);

        assertEquals(page, content1.size());
        assertEquals(page, content2.size());
        assertEquals(page, content6.size());
        assertEquals(0, content7.size(), () -> "남은 포스트 10개는 숨김처리되어 조회되지 않는다.");
        
    }

    @DisplayName("유저페이지에서는 해당 유저가 작성한 게시글만 조회된다.")
    @Test
    void userPageFeed() {

        User owner = User.builder()
                .username("username")
                .password("password")
                .role(PRESS)
                .build();
        userRepository.save(owner);

        //3 not owner's posts
        for(int i=0;i<3;i++) {
            Post post = Post.builder()
                    .isHidden(false)
                    .isAnonymous(false)
                    .build();
            postRepository.save(post);
        }

        //7 owner's posts
        for(int i=0;i<7;i++) {
            Post post = Post.builder()
                    .isHidden(false)
                    .isAnonymous(false)
                    .build();
            post.setUser(owner);
            postRepository.save(post);
        }

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(10L)
                .userPageFeedOption(UserPageFeedOption.WRITE)
                .soldOption(SoldOption.ALL)
                .build();

        List<FeedResultPostDto> content = feedRepository.searchUserPageBy(searchFeedConditionDto, owner,null, null);

        assertEquals(7, content.size());
    }

    @DisplayName("유저가 본인의 북마크를 조회한다.")
    @Test
    void bookmarkFeedSearch() {

        User owner = User.builder()
                .username("username")
                .password("password")
                .role(PRESS)
                .build();
        userRepository.save(owner);

        Post post = Post.builder()
                .isHidden(false)
                .isAnonymous(false)
                .build();
        postRepository.save(post);

        //3 not owner's posts
        for(int i=0;i<3;i++) {
            post = Post.builder()
                    .isHidden(false)
                    .isAnonymous(false)
                    .build();
            postRepository.save(post);
            Bookmark bookmark = new Bookmark(owner, post);
            bookmarkRepository.save(bookmark);
        }

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(3L)
                .userPageFeedOption(UserPageFeedOption.BOOKMARK)
                .soldOption(SoldOption.ALL)
                .build();

        List<FeedResultPostDto> result = feedRepository.searchUserPageByBookmark(searchFeedConditionDto, owner, owner, null);

        assertEquals(3, result.size());
    }

    @DisplayName("유저가 본인이 구매한 게시글을 조회한다.")
    @Test
    void purchasedFeedSearch() {

        User owner = User.builder()
                .username("username")
                .password("password")
                .role(PRESS)
                .build();
        userRepository.save(owner);

        Post post = Post.builder()
                .isHidden(false)
                .isAnonymous(false)
                .build();
        postRepository.save(post);

        //3 not owner's posts
        for(int i=0;i<3;i++) {
            post = Post.builder()
                    .isHidden(false)
                    .isAnonymous(false)
                    .build();
            postRepository.save(post);
            Purchase purchase = new Purchase(owner, post, 1000);
            purchaseRepository.save(purchase);
        }

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(3L)
                .userPageFeedOption(UserPageFeedOption.BUY)
                .soldOption(SoldOption.ALL)
                .build();

        List<FeedResultPostDto> result = feedRepository.searchUserPageByPurchase(searchFeedConditionDto, owner, owner, null);

        assertEquals(3, result.size());
    }
}
