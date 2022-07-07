package com.dope.breaking.service;

import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.FeedSearchConditionDto;
import com.dope.breaking.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchFeedService {

    private final FeedRepository feedRepository;

    public Page<FeedResultPostDto> searchFeedPagination(FeedSearchConditionDto feedSearchConditionDto, Pageable pageable) {

        if( feedSearchConditionDto.getVisibleSold() != null || feedSearchConditionDto.getDateFrom() != null || feedSearchConditionDto.getForLastMin() != null ) {
            return feedRepository.searchFilteredFeedBy(feedSearchConditionDto, pageable);
        } else {
            return feedRepository.searchDefaultFeedBy(feedSearchConditionDto, pageable);
        }
    }

}
