package com.dope.breaking.service;

import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedRequestDto;
import com.dope.breaking.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchFeedService {

    private final FeedRepository feedRepository;

    public Page<FeedResultPostDto> searchFeedPagination(SearchFeedRequestDto searchFeedRequestDto, Pageable pageable) {

        if( searchFeedRequestDto.getSoldPost() != null || searchFeedRequestDto.getDateFrom() != null || searchFeedRequestDto.getForLastMin() != null ) {
            return feedRepository.searchFilteredFeedBy(searchFeedRequestDto, pageable);
        } else {
            return feedRepository.searchDefaultFeedBy(searchFeedRequestDto, pageable);
        }
    }

}
