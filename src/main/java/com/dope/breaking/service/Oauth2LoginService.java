package com.dope.breaking.service;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.UserBriefInformationResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.auth.NotFoundUserAgent;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.security.jwt.DistinguishUserAgent;
import com.dope.breaking.security.jwt.JwtTokenProvider;
import com.dope.breaking.security.userInfoDto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class Oauth2LoginService {

    private final UserService userService;
    private final UserRepository userRepository;

    private final JwtTokenProvider jwtTokenProvider;

    private final DistinguishUserAgent distinguishUserAgent;

    private final RedisService redisService;

    public ResponseEntity<String> kakaoUserInfo(String accessToken){
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();

        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<HttpHeaders> kakaoRequest1 = new HttpEntity<>(headers);
        ResponseEntity<String> kakaoUserinfo = null;
        try {
            kakaoUserinfo = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.POST,
                    kakaoRequest1,
                    String.class
            );
        } catch (Exception e) {
            log.info(e.toString());
            throw new InvalidAccessTokenException();
        }
        return kakaoUserinfo;
    }

    public ResponseEntity<?> kakaoLogin(ResponseEntity<String> kakaoUserinfo, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ParseException, ServletException, IOException {
        String userAgent = Optional.ofNullable(httpServletRequest.getHeader("User-Agent")).orElseThrow( () -> new NotFoundUserAgent());
        log.info(userAgent);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(kakaoUserinfo.getBody());
        log.info(jsonObject.toJSONString());
        UserDto dto = new UserDto();
        dto.setUsername(jsonObject.get("id").toString() + "k");
        String kakao_account = jsonObject.get("kakao_account").toString();
        JSONObject infoObject = (JSONObject) jsonParser.parse(kakao_account);
        log.info(infoObject.toJSONString());
        if(infoObject.containsKey("email")){
            dto.setEmail(infoObject.get("email").toString());
        }else {
            log.info("이메일 정보를 불러올 수 없음");
            dto.setEmail(null);
        }
        String profile = infoObject.get("profile").toString();
        infoObject = (JSONObject) jsonParser.parse(profile);
        if(infoObject.containsKey("nickname")){
            dto.setFullname(infoObject.get("nickname").toString());
        } else {
            log.info("유저 이름을 불러올 수 없음");
            dto.setFullname(null);
        }

        if(infoObject.containsKey("profile_image_url")){
            dto.setProfileImgURL(infoObject.get("profile_image_url").toString());
        } else {
            log.info("기존 프로필 사진을 불러올 수 없음");
            dto.setProfileImgURL(null);
        }

        if (!userRepository.existsByUsername(dto.getUsername())) {
            log.info("기존 유저 정보가 없음.");
            return ResponseEntity.status(200).body(dto);
        } else {
            log.info("기존 유저 정보가 있음.");
            String userAgentType = distinguishUserAgent.extractUserAgent(userAgent);
            log.info(userAgentType);
            String accessToken = jwtTokenProvider.createAccessToken(dto.getUsername(), userAgentType);
            String refreshToken = jwtTokenProvider.createRefreshToken(dto.getUsername());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("authorization", accessToken);
            if(userAgentType.equals("WEB")) {
                Cookie cookie = new Cookie("authorization-refresh", refreshToken);
                cookie.setMaxAge(2 * 24 * 60 * 60); //2주
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                httpServletResponse.addCookie(cookie);
            }
            else{
                httpHeaders.set("authorization-refresh", refreshToken);
            }



            redisService.setDataWithExpiration(userAgentType + "_" +dto.getUsername(), refreshToken,2 * 604800L);
            User user = userRepository.findByUsername(dto.getUsername()).get();
            UserBriefInformationResponseDto userBriefInformationResponseDto  = UserBriefInformationResponseDto.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .profileImgURL(user.getOriginalProfileImgURL())
                    .balance(user.getBalance())
                    .build();
            return new ResponseEntity<UserBriefInformationResponseDto>(userBriefInformationResponseDto, httpHeaders, HttpStatus.OK);
        }
    }

    public ResponseEntity<String> googleUserInfo(String accessToken, String idToken){
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        headers.add("Authorization", "Bearer " + accessToken);
        ResponseEntity<String> GoogleUserinfo = null;
        try {
            HttpEntity profileRequest = new HttpEntity(headers);
            GoogleUserinfo = restTemplate.exchange(
                    "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken,
                    HttpMethod.GET,
                    profileRequest,
                    String.class
            );
        } catch (Exception e) {
            log.info(e.toString());
            throw new InvalidAccessTokenException();
        }
        return GoogleUserinfo;
    }



    public ResponseEntity<?> googleLogin(ResponseEntity<String> GoogleUserinfo, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ParseException, ServletException, IOException {
        String userAgent = Optional.ofNullable(httpServletRequest.getHeader("User-Agent")).orElseThrow( () -> new NotFoundUserAgent());

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(GoogleUserinfo.getBody());
        log.info(jsonObject.toJSONString());
        UserDto dto = new UserDto();
        dto.setUsername(jsonObject.get("sub").toString() + "g");

        if(jsonObject.containsKey("email")){
            dto.setEmail(jsonObject.get("email").toString());
        }else{
            log.info("유저 이메일을 불러올 수 없음");
            dto.setEmail(null);
        }
        if(jsonObject.containsKey("given_name")) {
            dto.setFullname(jsonObject.get("given_name").toString());
        }else{
            log.info("유저 이름을 불러올 수 없음");
            dto.setFullname(null);
        }
        if(jsonObject.containsKey("picture")){

            dto.setProfileImgURL(jsonObject.get("picture").toString());
        }else{
            log.info("기존 프로필 사진을 불러올 수 없음");
            dto.setProfileImgURL(null);
        }
        if (!userRepository.existsByUsername(dto.getUsername())) {
            log.info("유저 정보가 없음");
            return ResponseEntity.status(200).body(dto);
        } else {
            log.info("유저 정보가 있다.");
            String userAgentType = distinguishUserAgent.extractUserAgent(userAgent);
            String accessToken = jwtTokenProvider.createAccessToken(dto.getUsername(), userAgentType);
            String refreshToken = jwtTokenProvider.createRefreshToken(dto.getUsername());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("authorization", accessToken);
            httpHeaders.set("authorization-refresh", refreshToken);
            if(userAgentType.equals("WEB")) {
                Cookie cookie = new Cookie("authorization-refresh", refreshToken);
                cookie.setMaxAge(2 * 24 * 60 * 60); //2주
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                httpServletResponse.addCookie(cookie);
            }
            else{
                httpHeaders.set("authorization-refresh", refreshToken);
            }


            redisService.setDataWithExpiration(userAgentType + "_" + dto.getUsername(), refreshToken, 2 * 604800L);
            User user = userRepository.findByUsername(dto.getUsername()).get();
            UserBriefInformationResponseDto userBriefInformationResponseDto = UserBriefInformationResponseDto.builder()
                    .userId(user.getId())
                    .balance(user.getBalance())
                    .nickname(user.getNickname())
                    .profileImgURL(user.getOriginalProfileImgURL())
                    .build();
            return new ResponseEntity<UserBriefInformationResponseDto>(userBriefInformationResponseDto, httpHeaders, HttpStatus.OK);
        }
    }
}
