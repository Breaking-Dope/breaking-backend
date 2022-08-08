package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.exception.user.NoPermissionException;
import com.dope.breaking.repository.FeedRepository;
import com.dope.breaking.repository.PostRepository;
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
        Assertions.assertEquals(2, result.size());
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
}
