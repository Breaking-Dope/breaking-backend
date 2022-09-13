package com.dope.breaking.service;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SearchMissionConditionDto {

    private SortStrategy sortStrategy;

    private Boolean isMissionOnGoing;

    @Builder
    public SearchMissionConditionDto(SortStrategy sortStrategy, Boolean isMissionOnGoing) {
        this.sortStrategy = sortStrategy;
        this.isMissionOnGoing = isMissionOnGoing;
    }
}
