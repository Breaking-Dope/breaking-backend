package com.dope.breaking.security.controller;


import com.dope.breaking.security.jwt.JwtTokenProvider;
import com.dope.breaking.security.userInfoDto.InvalidAccessTokenResponse;
import com.dope.breaking.security.userInfoDto.UserDto;
import com.dope.breaking.domain.user.User;
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

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/oauth/sign-in")
@RestController
public class Oauth2LoginController {
    private final UserService userService;

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/naver")
    public ResponseEntity<?> naverOAuthRedirect(@RequestBody Map<String, String> accessToken) throws ParseException, JsonProcessingException, ParseException {
        // RestTemplate 인스턴스 생성
        RestTemplate restTemplate = new RestTemplate();
        String token = accessToken.get("accessToken");
        log.info("access token : {} ", token);

        JSONParser jsonParser = new JSONParser();
        //header를 생성해서 access token을 넣어주고
        HttpHeaders profileRequestHeader = new HttpHeaders();
        profileRequestHeader.add("Authorization", "Bearer " + token);

        HttpEntity<HttpHeaders> profileHttpEntity = new HttpEntity<>(profileRequestHeader);

        // profile api로 생성해둔 헤더를 담아서 요청을 보냅니다.
        ResponseEntity<String> profileResponse = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.POST,
                profileHttpEntity,
                String.class
        );

        JSONObject jsonObject = (JSONObject) jsonParser.parse(profileResponse.getBody());
        log.info(jsonObject.toJSONString());
        String userinfo = jsonObject.get("response").toString();
        JSONObject jsonObject1 = (JSONObject) jsonParser.parse(userinfo);
        UserDto dto = new UserDto();
        dto.setFullname(jsonObject1.get("name").toString());
        dto.setUsername(jsonObject1.get("id").toString());
        dto.setEmail(jsonObject1.get("email").toString());
        log.info(dto.toString());
        User user = userService.findByUsername(dto.getUsername()).orElse(null);
        if (user == null) {
            log.info("유저 정보가 없음");
            return ResponseEntity.status(200).body(dto);
        } else {
            log.info("유저 정보 있다.");
            String accessjwt = jwtTokenProvider.createAccessToken(user.getUsername());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", accessjwt);
            return new ResponseEntity<String>("토큰 발행", httpHeaders, HttpStatus.OK);
        }
    }


    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoOauthRedirect(@RequestBody Map<String, String> accessToken) throws ParseException {
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        String token = accessToken.get("accessToken");

        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<HttpHeaders> kakaoRequest1 = new HttpEntity<>(headers);
        ResponseEntity<String> kakaoUserinfo;
        try {
            kakaoUserinfo = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.POST,
                    kakaoRequest1,
                    String.class
            );
        } catch (Exception e) {
            log.info(e.toString());
            InvalidAccessTokenResponse invalidAccessTokenResponse = new InvalidAccessTokenResponse();
            invalidAccessTokenResponse.setAccessToken(token);
            invalidAccessTokenResponse.setErrorMessage("유효하지 않은 AccessToken");
            invalidAccessTokenResponse.setIdToken("Kakao에서는 필요하지 않는 Token");
            return new ResponseEntity<InvalidAccessTokenResponse>(invalidAccessTokenResponse, HttpStatus.NOT_ACCEPTABLE); //유효하지 않은 토큰이라고 에러메시지 표출.
        }
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(kakaoUserinfo.getBody());
        log.info(jsonObject.toJSONString());
        UserDto dto = new UserDto();
        dto.setUsername(jsonObject.get("id").toString());
        String kakao_account = jsonObject.get("kakao_account").toString();
        JSONObject jsonObject1 = (JSONObject) jsonParser.parse(kakao_account);
        log.info(jsonObject1.toJSONString());
        try {
            dto.setEmail(jsonObject1.get("email").toString());
        } catch (Exception e) {
            log.info("이메일 정보를 불러올 수 없음");
            dto.setEmail(null);
        }
        String profile = jsonObject1.get("profile").toString();
        jsonObject1 = (JSONObject) jsonParser.parse(profile);
        try {
            dto.setFullname(jsonObject1.get("nickname").toString());
        } catch (Exception e) {
            log.info("유저 이름을 불러올 수 없음");
            dto.setFullname(null);
        }
        try {
            dto.setProfileImgURL(jsonObject1.get("profile_image_url").toString());
        } catch (Exception e) {
            log.info("기존 프로필 사진을 불러올 수 없음");
            dto.setProfileImgURL(null);
        }

        if (!userService.existByUsername(dto.getUsername())) {
            log.info("기존 유저 정보가 없음.");
            return ResponseEntity.status(200).body(dto);
        } else {
            log.info("기존 유저 정보가 있음.");
            String accessjwt = jwtTokenProvider.createAccessToken(dto.getUsername());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", accessjwt);
            return new ResponseEntity<String>("토큰 발행", httpHeaders, HttpStatus.OK);
        }
    }


    @PostMapping("/google")
    public ResponseEntity<?> googleOauthRedirect(@RequestBody Map<String, String> accessToken) throws ParseException {
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        String token = accessToken.get("accessToken");
        String idtoken = accessToken.get("idToken");

        headers.add("Authorization", "Bearer " + token);
        ResponseEntity<String> GoogleUserinfo;
        try {
            HttpEntity profileRequest = new HttpEntity(headers);
            GoogleUserinfo = restTemplate.exchange(
                    "https://oauth2.googleapis.com/tokeninfo?id_token=" + idtoken,
                    HttpMethod.GET,
                    profileRequest,
                    String.class
            );
        } catch (Exception e) {
            log.info(e.toString());
            InvalidAccessTokenResponse invalidAccessTokenResponse = new InvalidAccessTokenResponse();
            invalidAccessTokenResponse.setAccessToken(token);
            invalidAccessTokenResponse.setIdToken(idtoken);
            invalidAccessTokenResponse.setErrorMessage("유효하지 않는 토큰 존재.");

            return new ResponseEntity<InvalidAccessTokenResponse>(invalidAccessTokenResponse, HttpStatus.NOT_ACCEPTABLE); //유효하지 않은 토큰이라고 에러메시지 표출.
        }
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(GoogleUserinfo.getBody());
        log.info(jsonObject.toJSONString());
        UserDto dto = new UserDto();
        dto.setUsername(jsonObject.get("sub").toString());
        try {
            dto.setEmail(jsonObject.get("email").toString());
        } catch (Exception e) {
            log.info("이메일 정보를 불러올 수 없음");
            dto.setEmail(null);
        }
        try {
            dto.setFullname(jsonObject.get("given_name").toString());
        } catch (Exception e) {
            log.info("유저 이름을 불러올 수 없음");
            dto.setFullname(null);
        }
        try {
            dto.setProfileImgURL(jsonObject.get("picture").toString());
        } catch (Exception e) {
            log.info("기존 프로필 사진을 불러올 수 없음");
            dto.setProfileImgURL(null);
        }
        if (!userService.existByUsername(dto.getUsername())) {
            log.info("유저 정보가 없음");
            return ResponseEntity.status(200).body(dto);
        } else {
            log.info("유저 정보가 있다.");
            String accessjwt = jwtTokenProvider.createAccessToken(dto.getUsername());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", accessjwt);
            return new ResponseEntity<String>("토큰 발행", httpHeaders, HttpStatus.OK);
        }
    }
}
