package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.exception.post.NoSuchPostException;
import com.dope.breaking.repository.FeedRepository;
import com.dope.breaking.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchFeedService {

    private final FeedRepository feedRepository;
    private final PostRepository postRepository;

    public List<FeedResultPostDto> searchFeed(SearchFeedConditionDto searchFeedConditionDto) {

        Post cursorPost = null;
        if(searchFeedConditionDto.getCursorId() != null && searchFeedConditionDto.getCursorId() != 0) {
            cursorPost = postRepository.findById(searchFeedConditionDto.getCursorId()).orElseThrow(NoSuchPostException::new);
        }

        if(searchFeedConditionDto.getForLastMin() != null) {
            searchFeedConditionDto.setDateFrom(LocalDateTime.now().minusMinutes(searchFeedConditionDto.getForLastMin()));
            searchFeedConditionDto.setDateTo(LocalDateTime.now());
        }

        return feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost);
    }

}
