package com.dope.breaking.api;


import com.dope.breaking.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BookmarkAPI {

    private final BookmarkService bookmarkService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/{postId}/bookmark")
    public ResponseEntity bookmarkPost(Principal principal, @PathVariable long postId){
        return bookmarkService.bookmarkPost(principal.getName(), postId);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/post/{postId}/bookmark")
    public ResponseEntity unbookmarkPost(Principal principal, @PathVariable long postId){
        return bookmarkService.unbookmarkPost(principal.getName(), postId);
    }
}
