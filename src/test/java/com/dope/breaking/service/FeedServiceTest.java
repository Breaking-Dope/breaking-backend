package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.dto.user.SearchUserResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.pagination.InvalidCursorException;
import com.dope.breaking.exception.user.LoginRequireException;
import com.dope.breaking.exception.user.NoPermissionException;
import com.dope.breaking.repository.*;
import com.dope.breaking.repository.FeedRepository;
import com.dope.breaking.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeedServiceTest {

    @Mock
    private FeedRepository feedRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private BookmarkRepository bookmarkRepository;
    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private SearchFeedService feedService;

    @DisplayName("유저 피드 조회가 정상적으로 작동한다.")
    @Test
    void searchUserPageTest() {

        //given
        User owner = new User();
        Post post1 = Post.builder()
                .title("post1")
                .build();
        post1.setUser(owner);
        Post post2 = Post.builder()
                .title("post2")
                .build();
        post2.setUser(owner);

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto.builder()
                .userPageFeedOption(UserPageFeedOption.WRITE)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        List<FeedResultPostDto> dummy = new ArrayList<>();
        FeedResultPostDto content1 = new FeedResultPostDto();
        content1.setTitle("post1");
        dummy.add(content1);
        FeedResultPostDto content2 = new FeedResultPostDto();
        content2.setTitle("post2");
        dummy.add(content2);
        given(feedRepository.searchUserPageBy(searchFeedConditionDto, owner, null, null)).willReturn(dummy);

        //when
        List<FeedResultPostDto> result = feedService.searchUserFeed(searchFeedConditionDto, 1L, null, null);

        //then
        assertEquals(2, result.size());
    }

    @DisplayName("다른 유저의 북마크 리스트를 조회할 때, 예외가 발생한다.")
    @Test
    void failureWhenOtherUserSearchOtherUserBookmarkList() {

        //given

        User guest = User.builder()
                .username("guestUsername")
                .build();

        User owner = User.builder()
                .username("ownerUsername")
                .build();

        Post post = Post.builder()
                .title("post1")
                .build();
        post.setUser(owner);


        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto.builder()
                .userPageFeedOption(UserPageFeedOption.BOOKMARK)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(userRepository.findByUsername("guestUsername")).thenReturn(Optional.of(guest));

        //when, then
        Assertions.assertThrows(NoPermissionException.class, ()->feedService.searchUserFeed(searchFeedConditionDto, 1L, "guestUsername", null));

    }

    @DisplayName("다른 유저의 구매 리스트를 조회할 때, 예외가 발생한다.")
    @Test
    void failureWhenOtherUserSearchOtherUserPurchaseList() {

        //given

        User guest = User.builder()
                .username("guestUsername")
                .build();

        User owner = User.builder()
                .username("ownerUsername")
                .build();

        Post post = Post.builder()
                .title("post1")
                .build();
        post.setUser(owner);


        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto.builder()
                .userPageFeedOption(UserPageFeedOption.BUY)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(userRepository.findByUsername("guestUsername")).thenReturn(Optional.of(guest));

        //when, then
        Assertions.assertThrows(NoPermissionException.class, ()->feedService.searchUserFeed(searchFeedConditionDto, 1L, "guestUsername", null));

    }

    @DisplayName("로그인 하지 않은 유저가 북마크 리스트를 조회할 때, 예외가 발생한다.")
    @Test
    void failureWhenGuestSearchOtherUserBookmarkList() {

        User owner = User.builder()
                .username("ownerUsername")
                .build();

        Post post = Post.builder()
                .title("post1")
                .build();
        post.setUser(owner);


        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto.builder()
                .userPageFeedOption(UserPageFeedOption.BOOKMARK)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        //when, then
        Assertions.assertThrows(LoginRequireException.class, ()->feedService.searchUserFeed(searchFeedConditionDto, 1L, null, null));

    }

    @DisplayName("로그인 하지 않은 유저가 구매 리스트를 조회할 때, 예외가 발생한다.")
    @Test
    void failureWhenGuestSearchOtherUserPurchaseList() {

        User owner = User.builder()
                .username("ownerUsername")
                .build();

        Post post = Post.builder()
                .title("post1")
                .build();
        post.setUser(owner);


        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto.builder()
                .userPageFeedOption(UserPageFeedOption.BUY)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        //when, then
        Assertions.assertThrows(LoginRequireException.class, ()->feedService.searchUserFeed(searchFeedConditionDto, 1L, null, null));

    }

    @DisplayName("메인 피드에서 익명 게시글은, user 정보가 null로 표기된다.")
    @Test
    void setNullAnonymousPostInMainFeed() {

        //given
        User owner = new User();
        owner.setRequestFields("URL", "URL", "owner", "01012345678", "email@naver.com", "Jinu", "msg", "username", Role.USER);

        Post post = Post.builder()
                .title("post1")
                .build();
        post.setUser(owner);

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto.builder()
                .userPageFeedOption(UserPageFeedOption.WRITE)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        List<FeedResultPostDto> dummy = new ArrayList<>();
        FeedResultPostDto content = new FeedResultPostDto();
        dummy.add(content);

        given(feedRepository.searchUserPageBy(searchFeedConditionDto, owner, null, null)).willReturn(dummy);

        //when
        List<FeedResultPostDto> result = feedService.searchUserFeed(searchFeedConditionDto, 1L, null, null);

        //then
        assertEquals(1, result.size());
        assertNull(result.get(0).getUser());

    }

    @DisplayName("+로 입력받은 문자열을 공백으로 replace하여 피드를 검색한다.")
    @Test
    void searchWithString() {

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto.builder()
                .userPageFeedOption(UserPageFeedOption.WRITE)
                .searchKeyword("좋은+아침")
                .build();

        List<FeedResultPostDto> dummy = new ArrayList<>();
        FeedResultPostDto content = new FeedResultPostDto();
        content.setIsAnonymous(false);
        dummy.add(content);

        given(feedRepository.searchFeedBy(searchFeedConditionDto, null, null)).willReturn(dummy);

        //when
        feedService.searchMainFeed(searchFeedConditionDto, null, null);

        //then
        assertEquals("좋은 아침", searchFeedConditionDto.getSearchKeyword());

    }

    @DisplayName("유저가 bookmark한 게시글은, isBookmarked가 true로 반환된다.")
    @Test
    void feedIsBookmarked() {

        //given
        User me = new User();
        me.setRequestFields("URL", "URL", "owner", "01012345678", "email@naver.com", "Jinu", "msg", "username", Role.USER);

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(1L)
                .soldOption(SoldOption.ALL)
                .build();


        when(userRepository.findByUsername(me.getUsername())).thenReturn(Optional.of(me));

        List<FeedResultPostDto> dummy = new ArrayList<>();
        FeedResultPostDto content = FeedResultPostDto.builder()
                .isAnonymous(false)
                .build();
        dummy.add(content);
        given(feedRepository.searchFeedBy(searchFeedConditionDto, null, me)).willReturn(dummy);
        given(bookmarkRepository.existsByUserAndPostId(me, null)).willReturn(true);
        given(postLikeRepository.existsByUserAndPostId(me, null)).willReturn(false);

        //when
        List<FeedResultPostDto> result = feedService.searchMainFeed(searchFeedConditionDto, me.getUsername(), null);

        //then
        assertTrue(result.get(0).getIsBookmarked());

    }

    @DisplayName("유저가 like한 게시글은, isLiked가 true로 반환된다.")
    @Test
    void feedIsLiked() {

        //given
        User me = new User();
        me.setRequestFields("URL", "URL", "owner", "01012345678", "email@naver.com", "Jinu", "msg", "username", Role.USER);

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto
                .builder()
                .size(1L)
                .soldOption(SoldOption.ALL)
                .build();


        when(userRepository.findByUsername(me.getUsername())).thenReturn(Optional.of(me));

        List<FeedResultPostDto> dummy = new ArrayList<>();
        FeedResultPostDto content = FeedResultPostDto.builder()
                .isAnonymous(false)
                .build();
        dummy.add(content);
        given(feedRepository.searchFeedBy(searchFeedConditionDto, null, me)).willReturn(dummy);
        given(bookmarkRepository.existsByUserAndPostId(me, null)).willReturn(false);
        given(postLikeRepository.existsByUserAndPostId(me, null)).willReturn(true);

        //when
        List<FeedResultPostDto> result = feedService.searchMainFeed(searchFeedConditionDto, me.getUsername(), null);

        //then
        assertTrue(result.get(0).getIsLiked());

    }

    @DisplayName("유저 검색이 정상적으로 작동한다.")
    @Test
    void userSearchTest() {

        String username = "username";
        User user = User.builder().username(username).build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        List<SearchUserResponseDto> returnResult = new ArrayList<>();
        returnResult.add(SearchUserResponseDto.builder().build());
        when(userRepository.searchUserBy(user, username, null, 1L)).thenReturn(returnResult);

        List<SearchUserResponseDto> result = feedService.searchUser(user.getUsername(), username, null, 1L);

        assertEquals(1, result.size());
    }

    @DisplayName("유저 검색에서 유효하지 않은 username을 입력하면 예외가 발생한다.")
    @Test
    void userSearchInvalidUsername() {

        Mockito.when(userRepository.findByUsername("wrongUsername")).thenThrow(InvalidAccessTokenException.class);
        assertThrows(InvalidAccessTokenException.class, ()-> feedService.searchUser("wrongUsername", "someUser", null, 1L));
    }

    @DisplayName("유저 검색에서 유효하지 않은 cursorId을 입력하면 예외가 발생한다.")
    @Test
    void userSearchInvalidCursor() {

        Mockito.when(userRepository.findById(100L)).thenThrow(InvalidCursorException.class);
        assertThrows(InvalidCursorException.class, ()-> feedService.searchUser(null, "someUser", 100L, 1L));
    }

    @DisplayName("유저 검색 시, 팔로잉 중인 유저는 isFolowing이 true로 반환된다.")
    @Test
    void isFollowingSearchedUser() {

        User me = User.builder().username("meUserName").build();
        User searchedUser = User.builder().username("searchedUserUserName").build();

        when(userRepository.findByUsername(me.getUsername())).thenReturn(Optional.of(me));
        when(followRepository.existsFollowsByFollowedIdAndFollowingId(me.getId(), searchedUser.getId())).thenReturn(true);

        List<SearchUserResponseDto> returnResult = new ArrayList<>();
        returnResult.add(SearchUserResponseDto.builder().build());
        when(userRepository.searchUserBy(me, "searchedUserUserName", null, 1L)).thenReturn(returnResult);

        List<SearchUserResponseDto> result = feedService.searchUser(me.getUsername(), "searchedUserUserName", null, 1L);

        assertTrue(result.get(0).getIsFollowing());

    }
}
