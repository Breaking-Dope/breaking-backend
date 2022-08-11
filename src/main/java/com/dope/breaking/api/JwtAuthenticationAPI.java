package com.dope.breaking.api;

import com.dope.breaking.dto.user.UserBriefInformationResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.auth.InvalidRefreshTokenException;
import com.dope.breaking.exception.user.NoSuchUserException;
import com.dope.breaking.security.jwt.JwtTokenProvider;
import com.dope.breaking.service.JwtAuthenticationService;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@RestController
public class JwtAuthenticationAPI {

    private final UserService userService;

    private final JwtAuthenticationService jwtAuthenticationService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/oauth2/validate-jwt")
    public ResponseEntity<UserBriefInformationResponseDto> validateJwt(Principal principal) {
        return ResponseEntity.ok().body(userService.userBriefInformation(principal.getName()));
    }

    @GetMapping("/reissue") //토큰 재발행 부분.
    public ResponseEntity<?> refreshTokenReissue(@RequestHeader(value = "authorization", required = true) String accessToken,
                                                 @RequestHeader(value = "authorization-refresh", required = false) String refreshToken,
                                                 HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {

        return jwtAuthenticationService.reissue(accessToken, refreshToken, httpServletRequest, httpServletResponse);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/oauth2/sign-out")
    public ResponseEntity logout(@RequestHeader(value = "authorization") String accessToken) throws IOException, ServletException {
        return jwtAuthenticationService.logout(accessToken);
    }
}
