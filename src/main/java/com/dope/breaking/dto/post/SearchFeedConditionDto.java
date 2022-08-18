package com.dope.breaking.dto.post;

import com.dope.breaking.domain.post.PostType;
import com.dope.breaking.service.SoldOption;
import com.dope.breaking.service.SortStrategy;
import com.dope.breaking.service.UserPageFeedOption;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Setter
public class SearchFeedConditionDto {

    private String searchKeyword;

    private String searchHashtag;

    private Long size;

    private SortStrategy sortStrategy;

    private SoldOption soldOption;

    private PostType postType;

    private UserPageFeedOption userPageFeedOption;

    private LocalDateTime dateFrom;

    private LocalDateTime dateTo;

    private Integer forLastMin;

    @Builder
    public SearchFeedConditionDto(String searchKeyword, String searchHashtag, Long size, SortStrategy sortStrategy, SoldOption soldOption, PostType postType, UserPageFeedOption userPageFeedOption, LocalDateTime dateFrom, LocalDateTime dateTo, Integer forLastMin) {
        this.searchKeyword = searchKeyword;
        this.searchHashtag = searchHashtag;
        this.size = size;
        this.sortStrategy = sortStrategy;
        this.soldOption = soldOption;
        this.postType = postType;
        this.userPageFeedOption = userPageFeedOption;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.forLastMin = forLastMin;
    }
}

