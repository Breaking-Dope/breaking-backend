package com.dope.breaking.repository;

import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedRepositoryCustom {

    Page<FeedResultPostDto> searchDefaultFeedBy(SearchFeedConditionDto searchFeedConditionDto, Pageable pageable);

    Page<FeedResultPostDto> searchFilteredFeedBy(SearchFeedConditionDto searchFeedConditionDto, Pageable pageable);

}
