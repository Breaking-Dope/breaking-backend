package com.dope.breaking.service;

import com.dope.breaking.exception.auth.ExpiredRefreshTokenException;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.auth.InvalidRefreshTokenException;
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

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtAuthenticationService {

    private final JwtTokenProvider jwtTokenProvider;

    private final RedisService redisService;

    public ResponseEntity<?> reissue(String accessToken, String refreshToken) throws IOException {

        String getAccessToken = jwtTokenProvider.extractAccessToken(accessToken).orElse(null);
        String getRefreshToken = jwtTokenProvider.extractRefreshToken(refreshToken).orElse(null);

        if (refreshToken != null && jwtTokenProvider.validateToken(getRefreshToken) == false) {
            try {
                String username = jwtTokenProvider.getUsername(accessToken);
            } catch (ExpiredJwtException e) {
                throw new ExpiredRefreshTokenException(); //만료 에러.
            } catch (SecurityException | IllegalArgumentException | JwtException e) {
                throw new InvalidRefreshTokenException(); //유효하지 않은 예외.
            }
        } else if (getAccessToken != null && jwtTokenProvider.validateToken(getRefreshToken) == true) {
            String username = jwtTokenProvider.getUsername(getRefreshToken);
            String redisRefreshToken = redisService.getData(username);
            if (!getRefreshToken.equals(redisRefreshToken)) { //Redis에 저장된 Refresh토큰이 존재하지 않을 때.
                throw new InvalidRefreshTokenException();
            }
            if(jwtTokenProvider.validateToken(getAccessToken) == true) { //만일 유효한 엑세스토큰이라면 블랙리스트로 지정.
                Long expiration = jwtTokenProvider.getExpireTime(getAccessToken);
                redisService.setBlackListToken(getAccessToken, "BLACKLIST_ACCESSTOKEN_" + username, expiration); //엑세스 토큰 블랙리스트 저장
            }

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", jwtTokenProvider.createAccessToken(username));
            String newRefrsehToken = jwtTokenProvider.createRefreshToken(username);
            httpHeaders.set("Authorization-Refresh", newRefrsehToken);
            redisService.setDataWithExpiration(username, newRefrsehToken, 2 * 604800L);

            return ResponseEntity.status(HttpStatus.OK).headers(httpHeaders).build();
        }
        throw new InvalidRefreshTokenException();
    }


    public ResponseEntity<?> logout(String accessToken) throws IOException {
        String getAccessToken = jwtTokenProvider.extractAccessToken(accessToken).orElse(null);

        String username = jwtTokenProvider.getUsername(getAccessToken);
        redisService.deleteValues(username); //레디스에 저장된 refreshToken 삭제

        Long expiration = jwtTokenProvider.getExpireTime(getAccessToken);
        redisService.setBlackListToken(getAccessToken, "BLACKLIST_ACCESSTOKEN_" + username, expiration); //엑세스 토큰 블랙리스트 저장
        return ResponseEntity.ok().build();
    }
}
