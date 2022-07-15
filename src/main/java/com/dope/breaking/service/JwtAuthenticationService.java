package com.dope.breaking.service;

import com.dope.breaking.exception.auth.InvalidRefreshTokenException;
import com.dope.breaking.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RequiredArgsConstructor
@Service
public class JwtAuthenticationService {

    private final JwtTokenProvider jwtTokenProvider;

    private final RedisService redisService;



    public ResponseEntity<?> reissue(String accessToken, String refreshToken) throws IOException {

        String getAccessToken = jwtTokenProvider.extractAccessToken(accessToken).orElse(null);
        String getRefreshToken = jwtTokenProvider.extractRefreshToken(refreshToken).orElse(null);

        if (refreshToken != null && jwtTokenProvider.validateToken(getRefreshToken) == false) { //리플리쉬 토큰이 있고, 유효하지 않다면?
            throw new InvalidRefreshTokenException();
        }
        else if(getAccessToken != null && jwtTokenProvider.validateToken(getRefreshToken) == true){
            String username = jwtTokenProvider.getUsername(getRefreshToken);
            String redisRefreshToken = redisService.getData(username);
            if(!getRefreshToken.equals(redisRefreshToken)){
                throw new InvalidRefreshTokenException();
            }

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", jwtTokenProvider.createAccessToken(username));
            String newRefrsehToken = jwtTokenProvider.createRefreshToken(username);
            httpHeaders.set("Authorization-Refresh", newRefrsehToken);
            redisService.setDataWithExpiration(username, newRefrsehToken, 2 * 604800L );

            return ResponseEntity.status(HttpStatus.OK).headers(httpHeaders).build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
