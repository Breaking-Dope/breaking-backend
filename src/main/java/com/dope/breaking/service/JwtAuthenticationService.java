package com.dope.breaking.service;

import com.dope.breaking.exception.auth.ExpiredRefreshTokenException;
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
            log.info(username);
            String redisRefreshToken = redisService.getData(username);
            log.info(redisRefreshToken);
            if (!getRefreshToken.equals(redisRefreshToken)) { //Redis에 저장된 Refresh토큰이 존재하지 않을 때.
                throw new InvalidRefreshTokenException();
            }

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", jwtTokenProvider.createAccessToken(username));
            String newRefrsehToken = jwtTokenProvider.createRefreshToken(username);
            httpHeaders.set("Authorization-Refresh", newRefrsehToken);
            redisService.setDataWithExpiration(username, newRefrsehToken, 2 * 604800L);

            return ResponseEntity.status(HttpStatus.OK).headers(httpHeaders).build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
