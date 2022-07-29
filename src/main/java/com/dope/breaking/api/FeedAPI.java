package com.dope.breaking.api;

import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.service.SoldOption;
import com.dope.breaking.service.SearchFeedService;
import com.dope.breaking.service.SortStrategy;
import com.dope.breaking.service.UserPageFeedOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class FeedAPI {

    private final SearchFeedService searchFeedService;

    @GetMapping("/feed")
    public ResponseEntity<List<FeedResultPostDto>> searchFeed(
            @RequestParam(value="cursor") Long cursor,
            @RequestParam(value="size") Long size,
            @RequestParam(value="search", required = false) String searchKeyword,
            @RequestParam(value="sort", required = false) String sortStrategy,
            @RequestParam(value="sold-option", required = false, defaultValue = "ALL") String soldOption,
            @RequestParam(value="date-from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(value="date-to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(value="for-last-min", required = false) Integer forLastMin
    ) {

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto.builder()
                .searchKeyword(searchKeyword)
                .cursorId(cursor)
                .size(size)
                .sortStrategy(SortStrategy.findMatchedEnum(sortStrategy))
                .soldOption(SoldOption.findMatchedEnum(soldOption))
                .dateFrom(dateFrom)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .forLastMin(forLastMin)
                .build();

        return ResponseEntity.ok().body(searchFeedService.searchFeed(searchFeedConditionDto));

    }

    @GetMapping("/feed/user/{ownerId}/{userFeedPostOption}")
    public ResponseEntity<List<FeedResultPostDto>> searchUserFeed(
            @PathVariable("ownerId") Long ownerId,
            @PathVariable("userFeedPostOption") String userFeedPostOption,
            @RequestParam(value="cursor") Long cursor,
            @RequestParam(value="size") Long size,
            @RequestParam(value="sold-option", required = false, defaultValue = "ALL") String soldOption
    ) {

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto.builder()
                .cursorId(cursor)
                .size(size)
                .ownerId(ownerId)
                .soldOption(SoldOption.findMatchedEnum(soldOption))
                .userPageFeedOption(UserPageFeedOption.findMatchedEnum(userFeedPostOption))
                .build();

        return ResponseEntity.ok().body(searchFeedService.searchFeed(searchFeedConditionDto));

    }

}
