package com.dope.breaking.api;

import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.service.SoldOption;
import com.dope.breaking.service.SearchFeedService;
import com.dope.breaking.service.SortStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@RestController
public class FeedAPI {

    private final SearchFeedService searchFeedService;

    @GetMapping("/feed")
    public ResponseEntity<Page<FeedResultPostDto>> searchFeed(
            @RequestParam(value="page") int page,
            @RequestParam(value="size") int size,
            @RequestParam(value="search", required = false) String searchKeyword,
            @RequestParam(value="sort", required = false) String sortStrategy,
            @RequestParam(value="sold-option", required = false, defaultValue = "ALL") String soldOption,
            @RequestParam(value="date-from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(value="date-to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(value="for-last-min", required = false) Integer forLastMin) {

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto.builder()
                .searchKeyword(searchKeyword)
                .sortStrategy(SortStrategy.findMatchedEnum(sortStrategy))
                .soldOption(SoldOption.findMatchedEnum(soldOption))
                .dateFrom(dateFrom)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .forLastMin(forLastMin)
                .build();

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok().body(searchFeedService.searchFeed(searchFeedConditionDto, pageable));

    }

}
