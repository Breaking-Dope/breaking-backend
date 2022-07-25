package com.dope.breaking.api;


import com.dope.breaking.dto.post.DetailPostResponseDto;
import com.dope.breaking.dto.post.PostRequestDto;
import com.dope.breaking.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PostAPI {

    private final PostService postService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/post", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Long>> postCreate(Principal principal,
                                        @RequestPart(value = "mediaList", required = false) List<MultipartFile> files, @RequestPart(value = "data") String contentData) throws Exception {
        Long postId = postService.create(principal.getName(), contentData, files);
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("postId", postId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/post/{postId}")
    public ResponseEntity<Map<String, Long>> postModify(@PathVariable("postId") long postId, Principal principal, @RequestBody PostRequestDto postRequestDto) throws Exception {
        postService.modify(postId , principal.getName(), postRequestDto);
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("Modified postId", postId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    @GetMapping(value = "/post/{postId}")
    public ResponseEntity<DetailPostResponseDto> postRead(@PathVariable("postId") long postId, Principal principal){
        String crntUsername = null;
        if(principal != null){
            crntUsername = principal.getName();
        }
        return new ResponseEntity<DetailPostResponseDto>(postService.read(postId, crntUsername), HttpStatus.OK);
    }


    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/post/{postId}")
    public ResponseEntity postDelete(@PathVariable("postId") long postId, Principal principal){
        return postService.delete(postId, principal.getName());
    }

}
