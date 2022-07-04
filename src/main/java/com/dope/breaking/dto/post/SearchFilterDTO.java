package com.dope.breaking.dto.post;

import com.dope.breaking.service.SortFilter;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SearchFilterDTO {

    private final SortFilter sort;

    private final String searchKeyword;

    private final Boolean soldPost;

    private final LocalDate dateFrom;

    private final LocalDate dateTo;
}
