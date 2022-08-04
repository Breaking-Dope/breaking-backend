package com.dope.breaking.api;


import com.dope.breaking.exception.auth.AlreadyLoginException;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.service.Oauth2LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/oauth2/sign-in")
@RestController
public class Oauth2LoginAPI {

    private final Oauth2LoginService oauth2LoginService;

    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoOauthLogin(Principal principal, @RequestBody Map<String, String> accessToken) throws InvalidAccessTokenException, ParseException {
        if(principal != null) throw new AlreadyLoginException();
        String token = accessToken.get("accessToken");
        ResponseEntity<String> kakaoUserinfo = oauth2LoginService.kakaoUserInfo(token);
        return oauth2LoginService.kakaoLogin(kakaoUserinfo);
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleOauthLogin(Principal principal, @RequestBody Map<String, String> accessToken) throws InvalidAccessTokenException, ParseException {
        if(principal != null) throw new AlreadyLoginException();
        String token = accessToken.get("accessToken");
        String idtoken = accessToken.get("idToken");
        ResponseEntity<String> GoogleUserinfo = oauth2LoginService.googleUserInfo(token, idtoken);
        return oauth2LoginService.googleLogin(GoogleUserinfo);
    }
}