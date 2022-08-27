package com.dope.breaking.api;


import com.dope.breaking.dto.post.DetailPostResponseDto;
import com.dope.breaking.dto.post.PostRequestDto;
import com.dope.breaking.service.*;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PostAPI {

    private final PostService postService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/post", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Long>> postCreate(
            Principal principal,
            @RequestPart(value = "mediaList", required = false) List<MultipartFile> files,
            @RequestPart(value = "data") String contentData
    ) throws Exception {
        Long postId = postService.create(principal.getName(), contentData, files);
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("postId", postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/post/{postId}")
    public ResponseEntity<Map<String, Long>> postModify(
            Principal principal,
            @PathVariable("postId") long postId,
            @RequestBody PostRequestDto postRequestDto
    ) throws Exception {
        postService.modify(postId , principal.getName(), postRequestDto);
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("postId", postId);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping(value = "/post/{postId}")
    public ResponseEntity<DetailPostResponseDto> postRead(Principal principal, @PathVariable("postId") long postId){
        String username = null;
        if(principal != null){
            username = principal.getName();
        }
        return ResponseEntity.ok().body(postService.read(postId, username));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<Void> postDelete(Principal principal, @PathVariable("postId") long postId){
        postService.delete(postId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/{postId}/deactivate-purchase")
    public ResponseEntity<Void> deactivatePurchase(Principal principal, @PathVariable Long postId){
        postService.deactivatePurchase(principal.getName(),postId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/{postId}/activate-purchase")
    public ResponseEntity<Void> activatePurchase(Principal principal, @PathVariable Long postId){
        postService.activatePurchase(principal.getName(),postId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/{postId}/hide")
    public ResponseEntity<Void> hidePost(Principal principal, @PathVariable Long postId){
        postService.hidePost(principal.getName(),postId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/post/{postId}/hide")
    public ResponseEntity<Void> cancelHidePost(Principal principal, @PathVariable Long postId) {
        postService.cancelHidePost(principal.getName(), postId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/post/{postId}/download/selected-media")
    public ResponseEntity<FileSystemResource> downloadSelectedMedia(
            Principal principal,
            @PathVariable(value = "postId") Long postId,
            @RequestBody Map<String, String> mediaURL) throws IOException {
        String username = null;
        if(principal != null) username = principal.getName();
        return postService.downloadSelectedMedia(postId, mediaURL.get("mediaURL"), username);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/post/{postId}/download/all-media")
    public void downloadAllMedia(
            Principal principal,
            @PathVariable(value = "postId") long postId,
            HttpServletResponse httpServletResponse) throws IOException {
        String username = null;
        if(principal != null) {
            username = principal.getName();
        }
        postService.downloadAllMedia(postId, username, httpServletResponse);
    }
}
