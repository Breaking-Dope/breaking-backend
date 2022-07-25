package com.dope.breaking.dto.post;

import com.dope.breaking.service.SoldOption;
import com.dope.breaking.service.SortStrategy;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Setter
public class SearchFeedConditionDto {

    private String searchKeyword;

    private Long cursorId;

    private Long size;

    private SortStrategy sortStrategy;

    private SoldOption soldOption;

    private LocalDateTime dateFrom;

    private LocalDateTime dateTo;

    private Integer forLastMin;

    @Builder

    public SearchFeedConditionDto(String searchKeyword, Long cursorId, Long size, SortStrategy sortStrategy, SoldOption soldOption, LocalDateTime dateFrom, LocalDateTime dateTo, Integer forLastMin) {
        this.searchKeyword = searchKeyword;
        this.cursorId = cursorId;
        this.size = size;
        this.sortStrategy = sortStrategy;
        this.soldOption = soldOption;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.forLastMin = forLastMin;
    }
}

