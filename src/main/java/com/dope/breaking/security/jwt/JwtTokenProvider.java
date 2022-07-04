package com.dope.breaking.security.jwt;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component //빈 등록
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private  String secret;

    private final String accessheader = "Authorization";

    private static final String BEARER = "Bearer ";

    private final long accesstokenValidityInMilliseconds = 30 * 60 * 1000L; //엑세스 토큰 유효기간  엑세스 토큰 30분

    // JWT 토큰 생성
    @PostConstruct
    protected void init() {
        secret = Base64.getEncoder().encodeToString(secret.getBytes());
    }

    public String createAccessToken(String username) {
        Claims claims = Jwts.claims().setSubject(username); // JWT payload 에 저장되는 정보단위
        Date now = new Date();
        return Jwts.builder()
                .setSubject("AccessToken")// 제목 지정
                .setClaims(claims) //유저이름 지정.
                .setIssuedAt(now) // 토큰 발행 시간 정보
                .setExpiration(new Date(now.getTime() + accesstokenValidityInMilliseconds)) // set Expire Time
                .signWith(SignatureAlgorithm.HS256, secret)  // 사용할 암호화 알고리즘과 signature 에 들어갈 secret값 세팅
                .compact(); //토큰생성
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) throws IOException, ServletException {
        return Optional.ofNullable(request.getHeader(accessheader)).filter(accessToken -> accessToken.startsWith(BEARER)).map(accessToken -> accessToken.replace(BEARER, ""));
    }

    // 토큰에서 Username추출하는 과정.
    public String getUsername(String token) { //Username을 얻자.
        String username = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
        log.info("JwtProvider's getUsername method : {}", username);
        return username;
    }

    public boolean validateToken(String token) { //유효한가를 체크.
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            return true;
        } catch (SignatureException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}
