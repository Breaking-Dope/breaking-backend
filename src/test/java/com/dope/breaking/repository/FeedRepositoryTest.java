package com.dope.breaking.repository;

import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.hashtag.Hashtag;
import com.dope.breaking.domain.hashtag.HashtagType;
import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostLike;
import com.dope.breaking.domain.post.PostType;
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
    @Autowired HashtagRepository hashtagRepository;
    @Autowired CommentRepository commentRepository;
    @Autowired MissionRepository missionRepository;

    @Autowired EntityManager em;

    @DisplayName("다른 사람이 숨긴 게시글은, 유저 페이지에 나타나지 않는다.")
    @Test
    void hideHiddenPostInOtherUserPage() {

        User owner = new User();
        userRepository.save(owner);
        Post post = Post.builder()
                .isHidden(true)
                .build();
        post.setUser(owner);
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

        User owner = new User();
        userRepository.save(owner);
        Post post = Post.builder()
                .isHidden(true)
                .build();
        post.setUser(owner);
        postRepository.save(post);
        Bookmark bookmark = new Bookmark(owner, post);
        bookmarkRepository.save(bookmark);

        em.flush();

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(1L)
                .soldOption(SoldOption.ALL)
                .build();

        List<FeedResultPostDto> result = feedRepository.searchUserPageBy(searchFeedConditionDto, owner, owner, null);

        assertEquals(result.get(0).getPostId(), post.getId());
    }

    @DisplayName("익명 포스트는 다른 유저의 유저페이지에서 나타나지 않는다.")
    @Test
    void hideAnonymousPostInOtherUsersPage() {

        User me = new User();
        userRepository.save(me);

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

        List<FeedResultPostDto> result = feedRepository.searchUserPageBy(searchFeedConditionDto, user, me, null);

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
        post.setUser(user);
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
                .content("안녕하세요? 깻묵입니다. 잘 지내시죠?")
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

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(3L)
                .soldOption(SoldOption.ALL)
                .searchKeyword("깻묵")
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
                .content("본문 없음")
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

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(3L)
                .soldOption(SoldOption.ALL)
                .searchKeyword("오늘도 맑음")
                .build();

        List<FeedResultPostDto> result = feedRepository.searchFeedBy(searchFeedConditionDto, null, user);
        assertEquals(1, result.size());
    }

    @DisplayName("해시태그가 포함된 게시글을 검색한다.")
    @Test
    void SearchPostByHashtag() {

        User user = new User();
        userRepository.save(user);

        Post postWithHashtag = Post.builder()
                .build();
        postWithHashtag.setUser(user);
        postRepository.save(postWithHashtag);

        Hashtag hashtag = new Hashtag(postWithHashtag, null, HashtagType.POST, "해시태그");
        hashtagRepository.save(hashtag);

        Post otherPost = Post.builder()
                .title("제목이 없음")
                .content("본문 없음")
                .build();
        otherPost.setUser(user);
        postRepository.save(otherPost);

        em.flush();

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(3L)
                .soldOption(SoldOption.ALL)
                .searchHashtag("해시태그")
                .build();

        List<FeedResultPostDto> result = feedRepository.searchFeedBy(searchFeedConditionDto, null, user);
        assertEquals(1, result.size());
        assertEquals(postWithHashtag.getId(), result.get(0).getPostId());
    }

    @DisplayName("댓글에 해시태그가 포함된 게시글을 검색한다.")
    @Test
    void SearchPostByHashtagInComment() {

        User user = new User();
        userRepository.save(user);

        Post postWithHashtag = new Post();
        postWithHashtag.setUser(user);
        postRepository.save(postWithHashtag);

        Comment comment = Comment.builder()
                .user(user)
                .post(postWithHashtag)
                .build();
        commentRepository.save(comment);

        Hashtag hashtag = new Hashtag(postWithHashtag, null, HashtagType.POST, "해시태그");
        hashtagRepository.save(hashtag);

        Post otherPost = new Post();
        otherPost.setUser(user);
        postRepository.save(otherPost);

        Comment otherComment = Comment.builder()
                .user(user)
                .post(otherPost)
                .build();
        commentRepository.save(otherComment);

        em.flush();

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(3L)
                .soldOption(SoldOption.ALL)
                .searchHashtag("해시태그")
                .build();

        List<FeedResultPostDto> result = feedRepository.searchFeedBy(searchFeedConditionDto, null, user);
        assertEquals(1, result.size());
        assertEquals(postWithHashtag.getId(), result.get(0).getPostId());
    }

    @DisplayName("브레이킹 미션에 대한 포스트가 조회된다.")
    @Test
    void SearchPostByMission() {

        User user = new User();
        userRepository.save(user);
        User otherUser = new User();
        userRepository.save(otherUser);

        Mission mission = Mission.builder()
                .user(user)
                .build();
        missionRepository.save(mission);
        Mission otherMission = Mission.builder()
                .user(otherUser)
                .build();
        missionRepository.save(otherMission);

        //create mission post
        for(int i=0;i<15;i++) {
            Post post = Post.builder()
                    .isHidden(false)
                    .postType(PostType.MISSION)
                    .build();
            post.setUser(user);
            post.updateMission(mission);
            postRepository.save(post);
        }

        //create other mission post
        for(int i=0;i<5;i++) {
            Post post = Post.builder()
                    .isHidden(false)
                    .postType(PostType.MISSION)
                    .build();
            post.setUser(user);
            post.updateMission(otherMission);
            postRepository.save(post);
        }

        em.flush();

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(100L)
                .postType(PostType.MISSION)
                .build();

        List<FeedResultPostDto> result = feedRepository.searchFeedByMission(searchFeedConditionDto, mission, null, null);
        assertEquals(15, result.size());
    }

    @DisplayName("유저가 숨긴 미션 게시글은, 미션을 게시한 사람에게 나타난다.")
    @Test
    void missionPostDisplayToMissionOwner() {

        User missionOwner = new User();
        userRepository.save(missionOwner);
        User user = new User();
        userRepository.save(user);

        Mission mission = Mission.builder()
                .user(missionOwner)
                .build();
        missionRepository.save(mission);

        Post hiddenPost = Post.builder()
                .isHidden(true)
                .postType(PostType.MISSION)
                .build();
        hiddenPost.setUser(user);
        hiddenPost.updateMission(mission);
        postRepository.save(hiddenPost);

        em.flush();

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(100L)
                .postType(PostType.MISSION)
                .build();

        List<FeedResultPostDto> result = feedRepository.searchFeedByMission(searchFeedConditionDto, mission, null, missionOwner);

        assertEquals(hiddenPost.getId(), result.get(0).getPostId());
    }

}
