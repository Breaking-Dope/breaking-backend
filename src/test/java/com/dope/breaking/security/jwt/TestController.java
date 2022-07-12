package com.dope.breaking.security.jwt;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/hello")
    public ResponseEntity<?> hello() {
        System.out.println("hello");
        return ResponseEntity.ok().body("hello");
    }

}
