package com.dope.breaking.dto.post;

import com.dope.breaking.service.SortStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SearchFeedRequestDto {

    String searchKeyword;

    SortStrategy sortStrategy;

    private Boolean soldPost;

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private Integer forLastMin;

}
