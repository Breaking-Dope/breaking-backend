package com.dope.breaking.dto.post;

import com.dope.breaking.service.MyPagePostOption;
import com.dope.breaking.service.MyPageSoldOption;
import com.dope.breaking.service.SortStrategy;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
public class FeedSearchConditionDto {

    String searchKeyword;

    SortStrategy sortStrategy;

    private Boolean visibleSold;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private Integer forLastMin;

    private MyPagePostOption myPagePostOption;

    private MyPageSoldOption myPageSoldOption;

    @Builder
    public FeedSearchConditionDto(String searchKeyword, SortStrategy sortStrategy, Boolean visibleSold, LocalDate dateFrom, LocalDate dateTo, Integer forLastMin, MyPagePostOption myPagePostOption, MyPageSoldOption myPageSoldOption) {
        this.searchKeyword = searchKeyword;
        this.sortStrategy = sortStrategy;
        this.visibleSold = visibleSold;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.forLastMin = forLastMin;
        this.myPagePostOption = myPagePostOption;
        this.myPageSoldOption = myPageSoldOption;
    }
}

