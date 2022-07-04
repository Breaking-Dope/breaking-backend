package com.dope.breaking.api;

import com.dope.breaking.dto.post.SearchFeedResponseDto;
import com.dope.breaking.service.SearchFeedService;
import com.dope.breaking.service.SortFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class PostAPI {

    private final SearchFeedService searchFeedService;

    @GetMapping("/post/feed")
    public ResponseEntity<SearchFeedResponseDto> searchFeed(
            @RequestParam(value="page-size", required = false) int pageSize,
            @RequestParam(value="page-number", required = false) int pageNumber,
            @RequestParam(value="search", required = false) String searchKeyword,
            @RequestParam(value="sort", required = false) String sort,
            @RequestParam(value="sold-post", required = false) Boolean soldPost,
            @RequestParam(value="date-from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(value="date-to", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo){

        SortFilter sortFilter = SortFilter.findMatchedEnum(sort);

        searchFeedService.searchFeed(pageSize, pageNumber);
        if(sortFilter == null && searchKeyword == null && soldPost == null && dateFrom == null && dateTo == null) {
            // 기본 조회
            searchFeedService.searchFeed(pageSize, pageNumber);
        } else if(sortFilter != null && searchKeyword == null && soldPost == null && dateFrom == null && dateTo == null) {
            // 정렬 필터 조회
            searchFeedService.searchFeed(pageSize, pageNumber, sortFilter);
        }

        return ResponseEntity.ok().body(new SearchFeedResponseDto());

    }

}
