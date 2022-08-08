package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.post.NoSuchPostException;
import com.dope.breaking.repository.FeedRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchFeedService {

    private final FeedRepository feedRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<FeedResultPostDto> searchFeed(SearchFeedConditionDto searchFeedConditionDto, String username, Long cursorId) {

        User me = null;
        if(username != null) {
            me = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        }

        Post cursorPost = null;
        if(cursorId != null && cursorId != 0) {
            cursorPost = postRepository.findById(cursorId).orElseThrow(NoSuchPostException::new);
        }

        if(searchFeedConditionDto.getForLastMin() != null) {
            searchFeedConditionDto.setDateFrom(LocalDateTime.now().minusMinutes(searchFeedConditionDto.getForLastMin()));
            searchFeedConditionDto.setDateTo(LocalDateTime.now());
        }

        if(searchFeedConditionDto.getUserPageFeedOption() != null) {
            return feedRepository.searchUserPageBy(searchFeedConditionDto, cursorPost, me);
        } else {
            return feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost, me);
        }

    }

}
