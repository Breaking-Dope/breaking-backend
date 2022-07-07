package com.dope.breaking.service;

import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchFeedService {

    private final FeedRepository feedRepository;

    public Page<FeedResultPostDto> searchFeedPagination(SearchFeedConditionDto searchFeedConditionDto, Pageable pageable) {

        if( searchFeedConditionDto.getVisibleSold() != null || searchFeedConditionDto.getDateFrom() != null || searchFeedConditionDto.getForLastMin() != null ) {
            return feedRepository.searchFilteredFeedBy(searchFeedConditionDto, pageable);
        } else {
            return feedRepository.searchDefaultFeedBy(searchFeedConditionDto, pageable);
        }
    }

}
