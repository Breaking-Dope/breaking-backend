package com.dope.breaking.api;

import com.dope.breaking.dto.user.UserBriefInformationResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.auth.InvalidRefreshTokenException;
import com.dope.breaking.exception.user.NoSuchUserException;
import com.dope.breaking.security.jwt.JwtTokenProvider;
import com.dope.breaking.service.RedisService;
import com.dope.breaking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.json.simple.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@RestController
public class JwtAuthenticationAPI {

    private final UserService userService;

    private final JwtTokenProvider jwtTokenProvider;

    private final RedisService redisService;



    @PreAuthorize("isAuthenticated()")
    @PostMapping("/oauth2/validate-jwt")
    public ResponseEntity<UserBriefInformationResponseDto> validateJwt(Principal principal) {
        return ResponseEntity.ok().body(userService.userBriefInformation(principal.getName()));
    }

    @GetMapping("/reissue") //토큰 재발행 부분.
    public ResponseEntity<?> refreshTokenReissue(@RequestHeader(value = "Authorization", required = true) String accessToken,
                                                 @RequestHeader(value = "Authorization-Refresh", required = true) String refreshToken) throws IOException {

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
