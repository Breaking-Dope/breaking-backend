package com.dope.breaking.dto.post;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchFeedRequestDTO {

    private String sort;

    private Boolean soldPost;

    private LocalDateTime dateFrom;

    private LocalDateTime dateTo;

    private int forLastMin;
}
