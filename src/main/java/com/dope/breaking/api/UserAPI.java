package com.dope.breaking.api;

import com.dope.breaking.dto.user.*;


import com.dope.breaking.service.UserService;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
=======
import org.json.simple.JSONObject;
import org.springframework.http.HttpHeaders;
>>>>>>> d858f22 (feat: add JwtAuthenticationAPI for refrsh token reissue)
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class UserAPI {

    private final UserService userService;



    @GetMapping("/oauth2/sign-up/validate-phone-number/{phoneNumber}")
    public ResponseEntity<Void> validatePhoneNumber(@PathVariable String phoneNumber, Principal principal){

        String username = null;
        if(principal != null)  {
            username = principal.getName();
        }
        userService.validatePhoneNumber(phoneNumber, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/oauth2/sign-up/validate-email/{email}")
    public ResponseEntity<Void> validateEmail(@PathVariable String email, Principal principal){

        String username = null;
        if(principal != null)  {
            username = principal.getName();
        }
        userService.validateEmail(email, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/oauth2/sign-up/validate-nickname/{nickname}")
    public ResponseEntity<Void> validateNickname(@PathVariable String nickname, Principal principal){

        String username = null;
        if(principal != null)  {
            username = principal.getName();
        }
        userService.validateNickname(nickname, username);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAnonymous")
    @PostMapping(value = "/oauth2/sign-up", consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> signUp(
            @RequestPart String signUpRequest,
            @RequestPart (required = false) List<MultipartFile> profileImg) {
        return userService.signUp(signUpRequest, profileImg);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/profile", consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> profileUpdateConfirm(
            Principal principal,
            @RequestPart String updateRequest,
            @RequestPart (required = false) List<MultipartFile> profileImg) {

        userService.updateProfile(principal.getName(), updateRequest, profileImg);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<ProfileInformationResponseDto> profileInformation(Principal principal, @PathVariable Long userId){
        String userName = null;
        if (principal != null) {
            userName = principal.getName();
        }
        return ResponseEntity.ok().body(userService.profileInformation(userName, userId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile/detail")
    public ResponseEntity<FullUserInformationResponse> fullUserInformation(Principal principal) {
        return ResponseEntity.ok().body(userService.getFullUserInformation(principal.getName()));
    }


}
