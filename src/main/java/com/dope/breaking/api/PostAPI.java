package com.dope.breaking.api;


import com.dope.breaking.dto.post.PostCreateRequestDto;
import com.dope.breaking.dto.post.PostResType;
import com.dope.breaking.dto.post.PostResponse;
import com.dope.breaking.dto.post.SearchFeedResponseDto;
import com.dope.breaking.dto.response.MessageResponseDto;
import com.dope.breaking.service.PostService;
import com.dope.breaking.service.SearchFeedService;
import com.dope.breaking.service.SortFilter;
import com.dope.breaking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PostAPI {

    private final SearchFeedService searchFeedService;

    private final UserService userService;

    private final PostService postService;


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


    @PreAuthorize("isAuthenticated()")//인증 되었는가? //403 Fobbiden 반환.
    @PostMapping(value = "/post", consumes = {"multipart/form-data"})
    public ResponseEntity<?> PostCreate(Principal principal,
                                        @RequestPart(value = "mediaList") List<MultipartFile> files, @RequestPart(value = "data") @Valid PostCreateRequestDto postCreateRequestDto) {


        Optional<String> cntusername = Optional.ofNullable(principal.getName());
        Long postid;
        if (cntusername.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponseDto(PostResType.NOT_FOUND_USER.getMessage()));
        }//유저 정보 없으면 일치하지 않다고 반환하기.
        if (!userService.existByUsername(cntusername.get())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDto(PostResType.NOT_REGISTERED_USER.getMessage()));
        }
        try {
            postid = postService.create(cntusername.get(), postCreateRequestDto, files);

        } catch (Exception e) {
            log.info("게시글 등록에 실패함");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(PostResType.POST_FAILED.getMessage()));
        }
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("postId", postid);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class) //단일 컨트롤러에만 적용함.
    public ResponseEntity<Map<String, String>> ValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        List<ObjectError> errlist = e.getBindingResult().getAllErrors();
        for (ObjectError err : errlist) {
            FieldError fe = (FieldError) err;
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

}
