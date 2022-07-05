package com.dope.breaking.api;

import com.dope.breaking.dto.post.*;
import com.dope.breaking.dto.response.MessageResponseDto;
import com.dope.breaking.service.PostService;
import com.dope.breaking.service.SearchFeedService;
import com.dope.breaking.service.SortStrategy;
import com.dope.breaking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<Page<FeedResultPostDto>> searchFeed(
            @RequestParam(value="page") int page,
            @RequestParam(value="size") int size,
            @RequestParam(value="search", required = false) String searchKeyword,
            @RequestParam(value="sort-strategy", required = false) String sortStrategy,
            @RequestParam(value="sold-post", required = false) Boolean soldPost,
            @RequestParam(value="date-from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(value="date-to", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            @RequestParam(value="for-last-min", required = false) Integer forLastMin) {

        SearchFeedRequestDto searchFeedRequestDto = new SearchFeedRequestDto(searchKeyword, SortStrategy.findMatchedEnum(sortStrategy),
                soldPost, dateFrom, dateTo, forLastMin);

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok().body(searchFeedService.searchFeedPagination(searchFeedRequestDto, pageable));

    }


    @PreAuthorize("isAuthenticated()")//인증 되었는가? //403 Fobbiden 반환.
    @PostMapping(value = "/post")
    public ResponseEntity<?> PostCreate(Principal principal,
                                        @RequestPart(value = "mediaList", required = false) List<MultipartFile> files, @RequestPart(value = "data") @Valid PostCreateRequestDto postCreateRequestDto) {
        Long postid;
        if (principal ==  null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponseDto(PostResType.NOT_FOUND_USER.getMessage()));
        }//유저 정보 없으면 일치하지 않다고 반환하기.
        String cntusername = principal.getName();
        if (!userService.existByUsername(cntusername)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDto(PostResType.NOT_REGISTERED_USER.getMessage()));
        }
        try {
            postid = postService.create(cntusername, postCreateRequestDto, files);

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
