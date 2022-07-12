package com.dope.breaking.security.controller;


import com.dope.breaking.exception.auth.InvalidAccessTokenFromOauth2Exception;
import com.dope.breaking.security.jwt.JwtTokenProvider;
import com.dope.breaking.security.userInfoDto.InvalidAccessTokenResponse;
import com.dope.breaking.security.userInfoDto.UserDto;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.service.Oauth2LoginService;
import com.dope.breaking.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.Null;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/oauth2/sign-in")
@RestController
public class Oauth2LoginController {


    private final Oauth2LoginService oauth2LoginService;


    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoOauthLogin(@RequestBody Map<String, String> accessToken) throws InvalidAccessTokenFromOauth2Exception, ParseException {
        String token = accessToken.get("accessToken");
        ResponseEntity<String> kakaoUserinfo = oauth2LoginService.kakaoUserInfo(token);
        return oauth2LoginService.kakaoLogin(kakaoUserinfo);
    }


    @PostMapping("/google")
    public ResponseEntity<?> googleOauthLogin(@RequestBody Map<String, String> accessToken) throws InvalidAccessTokenFromOauth2Exception, ParseException {
        String token = accessToken.get("accessToken");
        String idtoken = accessToken.get("idToken");
        ResponseEntity<String> GoogleUserinfo = oauth2LoginService.googleUserInfo(token, idtoken);
        return oauth2LoginService.googleLogin(GoogleUserinfo);
    }
}
