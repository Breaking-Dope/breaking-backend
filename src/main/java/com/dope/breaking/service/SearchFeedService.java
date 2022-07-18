package com.dope.breaking.service;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.repository.FeedRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SearchFeedService {

    private final FeedRepository feedRepository;

    public Page<FeedResultPostDto> searchFeed(SearchFeedConditionDto searchFeedConditionDto, Pageable pageable) {

        if(searchFeedConditionDto.getForLastMin() != null) {
            searchFeedConditionDto.setDateFrom(LocalDateTime.now().minusMinutes(searchFeedConditionDto.getForLastMin()));
            searchFeedConditionDto.setDateTo(LocalDateTime.now());
        }
        return feedRepository.searchFeedBy(searchFeedConditionDto, pageable);
    }

}
