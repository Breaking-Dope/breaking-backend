package com.dope.breaking.service;

import com.dope.breaking.exception.auth.ExpiredRefreshTokenException;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.auth.InvalidRefreshTokenException;
import com.dope.breaking.exception.auth.NotFoundUserAgent;
import com.dope.breaking.security.jwt.DistinguishUserAgent;
import com.dope.breaking.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtAuthenticationService {

    private final JwtTokenProvider jwtTokenProvider;

    private final DistinguishUserAgent distinguishUserAgent;

    private final RedisService redisService;

    public HttpHeaders reissue(String accessToken, String refreshToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {

        String userAgent = Optional.ofNullable(httpServletRequest.getHeader("User-Agent")).orElseThrow(NotFoundUserAgent::new);

        String userAgentType = distinguishUserAgent.extractUserAgent(userAgent);
        String getAccessToken = jwtTokenProvider.extractAccessToken(accessToken).orElse(null);
        String getRefreshToken = userAgentType.equals("WEB") ? jwtTokenProvider.extractRefreshTokenFromCookie(httpServletRequest) : jwtTokenProvider.extractRefreshToken(refreshToken).orElse(null);

        if (refreshToken != null && !jwtTokenProvider.validateToken(getRefreshToken)) {
            try {
                String username = jwtTokenProvider.getUsername(accessToken);
            } catch (ExpiredJwtException e) {
                throw new ExpiredRefreshTokenException(); //만료 에러.
            } catch (SecurityException | IllegalArgumentException | JwtException e) {
                throw new InvalidRefreshTokenException(); //유효하지 않은 예외.
            }
        } else if (getAccessToken != null && jwtTokenProvider.validateToken(getRefreshToken)) {
            String username = jwtTokenProvider.getUsername(getRefreshToken);

            String redisRefreshToken = redisService.getData(userAgentType + "_" + username);
            if (!getRefreshToken.equals(redisRefreshToken)) { //Redis에 저장된 Refresh토큰이 존재하지 않을 때.
                throw new InvalidRefreshTokenException();
            }
            if (jwtTokenProvider.validateToken(getAccessToken)) { //만일 유효한 엑세스토큰이라면 블랙리스트로 지정.
                Long expiration = jwtTokenProvider.getExpireTime(getAccessToken);
                redisService.setBlackListToken(getAccessToken, "BLACKLIST_ACCESSTOKEN_" + username, expiration); //엑세스 토큰 블랙리스트 저장
            }

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("authorization", jwtTokenProvider.createAccessToken(username, userAgentType));
            String newRefrsehToken = jwtTokenProvider.createRefreshToken(username);
            if(userAgentType.equals("WEB")) {
                Cookie cookie = new Cookie("authorization-refresh", newRefrsehToken);
                cookie.setMaxAge(14 * 24 * 60 * 60); //2주
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                httpServletResponse.addCookie(cookie);
            }
            else{
                httpHeaders.set("authorization-refresh", newRefrsehToken);
            }
            redisService.setDataWithExpiration(userAgentType + "_" + username, newRefrsehToken, 2 * 604800L);

            return httpHeaders;
        }
        throw new InvalidRefreshTokenException();
    }


    public void logout(String accessToken) throws IOException {
        String getAccessToken = jwtTokenProvider.extractAccessToken(accessToken).orElse(null);

        String username = jwtTokenProvider.getUsername(getAccessToken);
        String userAgentType = jwtTokenProvider.getUserAgent(getAccessToken);
        log.info(userAgentType);
        redisService.deleteValues(userAgentType + "_" + username); //레디스에 저장된 refreshToken 삭제

        Long expiration = jwtTokenProvider.getExpireTime(getAccessToken);
        redisService.setBlackListToken(getAccessToken, "BLACKLIST_ACCESSTOKEN_" + username, expiration); //엑세스 토큰 블랙리스트 저장
    }
}
