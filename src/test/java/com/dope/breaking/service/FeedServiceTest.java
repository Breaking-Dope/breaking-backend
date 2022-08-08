package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
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
        User user = new User();
        Post post1 = Post.builder()
                .title("post1")
                .build();
        post1.setUser(user);
        Post post2 = Post.builder()
                .title("post2")
                .build();
        post2.setUser(user);

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto.builder()
                .userPageFeedOption(UserPageFeedOption.WRITE)
                .build();

        Mockito.lenient().when(userRepository.findByUsername(null)).thenReturn(Optional.of(user));
        Mockito.lenient().when(postRepository.findById(0L)).thenReturn(null);
        List<FeedResultPostDto> dummy = new ArrayList<>();
        FeedResultPostDto content1 = new FeedResultPostDto();
        content1.setTitle("post1");
        dummy.add(content1);
        FeedResultPostDto content2 = new FeedResultPostDto();
        content2.setTitle("post2");
        dummy.add(content2);
        given(feedRepository.searchUserPageBy(searchFeedConditionDto, null, null)).willReturn(dummy);

        //when
        List<FeedResultPostDto> result = feedService.searchFeed(searchFeedConditionDto, null, null);

        //then
        Assertions.assertEquals(2, result.size());
    }
}
