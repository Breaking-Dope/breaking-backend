package com.dope.breaking.api;

import com.dope.breaking.dto.post.*;

import com.dope.breaking.dto.post.PostRequestDto;
import com.dope.breaking.dto.post.PostResType;
import com.dope.breaking.dto.response.MessageResponseDto;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.service.PostService;
import com.dope.breaking.service.SearchFeedService;
import com.dope.breaking.service.SortStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PostAPI {

    private final SearchFeedService searchFeedService;

    private final UserRepository userRepository;

    private final PostService postService;

    @GetMapping("/post/feed")
    public ResponseEntity<Page<FeedResultPostDto>> searchFeed(
            @RequestParam(value="page") int page,
            @RequestParam(value="size") int size,
            @RequestParam(value="search", required = false) String searchKeyword,
            @RequestParam(value="sort-strategy", required = false) String sortStrategy,
            @RequestParam(value="visible-sold", required = false) Boolean visibleSold,
            @RequestParam(value="date-from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
            @RequestParam(value="date-to", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo,
            @RequestParam(value="for-last-min", required = false) Integer forLastMin) {

        SearchFeedConditionDto searchFeedConditionDto = SearchFeedConditionDto.builder()
                .searchKeyword(searchKeyword)
                .sortStrategy(SortStrategy.findMatchedEnum(sortStrategy))
                .visibleSold(visibleSold)
                .dateFrom(dateFrom)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .forLastMin(forLastMin)
                .build();

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok().body(searchFeedService.searchFeedPagination(searchFeedConditionDto, pageable));


    }


    @PreAuthorize("isAuthenticated()")//인증 되었는가? //403 Forbidden 반환.
    @PostMapping(value = "/post", consumes = {"multipart/form-data"})
    public ResponseEntity<?> postCreate(Principal principal,
                                        @RequestPart(value = "mediaList", required = false) List<MultipartFile> files, @RequestPart(value = "data") String contentData) throws JsonProcessingException {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponseDto(PostResType.NOT_FOUND_USER.getMessage()));
        }//유저 정보 없으면 일치하지 않다고 반환하기.
        if (!userRepository.existsByUsername(principal.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDto(PostResType.NOT_REGISTERED_USER.getMessage()));
        }

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PostRequestDto postRequestDto = mapper.readerFor(PostRequestDto.class).readValue(contentData);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<PostRequestDto>> violations = validator.validate(postRequestDto);

        Map<String,String> nullFieldMap = new LinkedHashMap<>();
        for (ConstraintViolation<PostRequestDto> violation : violations) {
            nullFieldMap.put(String.valueOf(violation.getPropertyPath()),violation.getMessage());
        }

        if(!nullFieldMap.isEmpty()){
            return ResponseEntity.badRequest().body(nullFieldMap);
        }

        Long postId;
        try {
            postId = postService.create(principal.getName(), postRequestDto, files);

        } catch (Exception e) {
            log.info("게시글 등록에 실패함");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(PostResType.POST_FAILED.getMessage()));
        }
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("postId", postId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/post/{postId}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> postModify(@PathVariable("postId") long postid, Principal principal,
                                        @RequestPart(value = "mediaList", required = false) List<MultipartFile> files, @RequestPart(value = "data") String contentData) throws JsonProcessingException {
        if (principal == null) { //인증된 사용자가 없다면 403반환.
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponseDto(PostResType.NOT_FOUND_USER.getMessage()));
        }//유저 정보 없으면 일치하지 않는다면 401반환
        if (!userRepository.existsByUsername(principal.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDto(PostResType.NOT_REGISTERED_USER.getMessage()));
        }//작성자와 수정을 시도하려는 자가 일치하지 않는다면 400 반환
        if(!postService.existByPostIdAndUserId(postid , userRepository.findByUsername(principal.getName()).get().getId())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(PostResType.NO_PERMISSION.getMessage()));
        }

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PostRequestDto postRequestDto = mapper.readerFor(PostRequestDto.class).readValue(contentData);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<PostRequestDto>> violations = validator.validate(postRequestDto);

        Map<String,String> nullFieldMap = new LinkedHashMap<>();
        for (ConstraintViolation<PostRequestDto> violation : violations) {
            nullFieldMap.put(String.valueOf(violation.getPropertyPath()),violation.getMessage());
        }
        if(!nullFieldMap.isEmpty()){
            return ResponseEntity.badRequest().body(nullFieldMap);
        }
        try {
            postService.modify(postid, postRequestDto, files);
        } catch (Exception e) {
            log.info("게시글 수정에 실패함");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(PostResType.POST_FAILED.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.OK).body("Post is Modified");
    }
}
