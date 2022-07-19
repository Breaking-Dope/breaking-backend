package com.dope.breaking.api;


import com.dope.breaking.repository.UserRepository;
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
    public ResponseEntity<?> postCreate(Principal principal,
                                        @RequestPart(value = "mediaList", required = false) List<MultipartFile> files, @RequestPart(value = "data") String contentData) throws Exception {
        Long postId = postService.create(principal.getName(), contentData, files);
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("postId", postId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/post/{postId}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> postModify(@PathVariable("postId") long postId, Principal principal,
                                        @RequestPart(value = "mediaList", required = false) List<MultipartFile> files, @RequestPart(value = "data") String contentData) throws Exception {
        postService.modify(postId, principal.getName(), contentData, files);
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("Modified postId", postId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
