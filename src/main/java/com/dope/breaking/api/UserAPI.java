package com.dope.breaking.api;

import com.dope.breaking.dto.user.*;


import com.dope.breaking.exception.auth.AlreadyLoginException;
import com.dope.breaking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class UserAPI {

    private final UserService userService;

    @GetMapping("/oauth2/sign-up/validate-phone-number/{phoneNumber}")
    public ResponseEntity<Void> validatePhoneNumber(Principal principal, @PathVariable String phoneNumber) {
        String username = null;
        if (principal != null) {
            username = principal.getName();
        }
        userService.validatePhoneNumber(phoneNumber, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/oauth2/sign-up/validate-email/{email}")
    public ResponseEntity<Void> validateEmail(Principal principal, @PathVariable String email) {
        String username = null;
        if (principal != null) {
            username = principal.getName();
        }
        userService.validateEmail(email, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/oauth2/sign-up/validate-nickname/{nickname}")
    public ResponseEntity<Void> validateNickname(Principal principal, @PathVariable String nickname) {
        String username = null;
        if (principal != null) {
            username = principal.getName();
        }
        userService.validateNickname(nickname, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/oauth2/sign-up", consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> signUp(
            Principal principal,
            @RequestPart String signUpRequest,
            @RequestPart(required = false) List<MultipartFile> profileImg,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException, IOException {
        if (principal != null) throw new AlreadyLoginException();
        return userService.signUp(signUpRequest, profileImg, httpServletRequest, httpServletResponse);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/oauth2/withdraw")
    public ResponseEntity<FullUserInformationResponse> signOut(Principal principal) {
        userService.withdraw(principal.getName());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/profile", consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> profileUpdateConfirm(
            Principal principal,
            @RequestPart String updateRequest,
            @RequestPart(required = false) List<MultipartFile> profileImg) {
        userService.updateProfile(principal.getName(), updateRequest, profileImg);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<ProfileInformationResponseDto> profileInformation(Principal principal, @PathVariable Long userId) {
        String username = null;
        if (principal != null) {
            username = principal.getName();
        }
        return ResponseEntity.ok().body(userService.profileInformation(username, userId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile/detail")
    public ResponseEntity<FullUserInformationResponse> fullUserInformation(Principal principal) {
        return ResponseEntity.ok().body(userService.getFullUserInformation(principal.getName()));
    }

}
