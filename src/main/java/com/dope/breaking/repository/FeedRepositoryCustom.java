package com.dope.breaking.repository;

import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.FeedSearchConditionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedRepositoryCustom {

    Page<FeedResultPostDto> searchDefaultFeedBy(FeedSearchConditionDto feedSearchConditionDto, Pageable pageable);

    Page<FeedResultPostDto> searchFilteredFeedBy(FeedSearchConditionDto feedSearchConditionDto, Pageable pageable);

}
