package com.dope.breaking.repository;

import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedRepositoryCustom {

    Page<FeedResultPostDto> searchDefaultFeedBy(SearchFeedRequestDto searchFeedRequestDto, Pageable pageable);

    Page<FeedResultPostDto> searchFilteredFeedBy(SearchFeedRequestDto searchFeedRequestDto, Pageable pageable);

}
