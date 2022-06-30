package com.dope.breaking.api;

import com.dope.breaking.dto.post.SearchFeedResponseDTO;
import com.dope.breaking.dto.SearchFilterDTO;
import com.dope.breaking.service.SearchFeedService;
import com.dope.breaking.service.SortFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class PostAPI {

    private final SearchFeedService searchFeedService;

    @GetMapping("/post/feed")
    public ResponseEntity<SearchFeedResponseDTO> GetFeedWithoutFilters(
            @RequestParam(value="page-size", required = false) int pageSize,
            @RequestParam(value="page-number", required = false) int pageNumber,
            @RequestParam(value="search", required = false) String searchKeyword,
            @RequestParam(value="sort", required = false, defaultValue = "CHRONOLOGICAL") String sort,
            @RequestParam(value="sold-post", required = false) Boolean soldPost,
            @RequestParam(value="date-from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(value="date-to", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo){

        if(sort.equals("CHRONOLOGICAL") && searchKeyword == null && soldPost == null && dateFrom == null && dateTo == null) {
            // 기본 조회
            searchFeedService.searchFeed(pageSize, pageNumber);
        }

        return ResponseEntity.ok().body(new SearchFeedResponseDTO());

    }

}
