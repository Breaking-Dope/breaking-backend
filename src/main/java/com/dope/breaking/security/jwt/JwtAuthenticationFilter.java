package com.dope.breaking.security.jwt;

import com.dope.breaking.security.userDetails.PrincipalDetailsService;
import com.dope.breaking.service.RedisService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {//모든 서블릿 컨테이너에서 요청 디스패치당 단일 실행을 보장하는 것을 목표로 하는 필터 기본 클래스
    private final JwtTokenProvider jwtTokenProvider;

    private final PrincipalDetailsService principalDetailsService;


    private final RedisService redisService;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (request.getRequestURI().equals("/reissue")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = jwtTokenProvider.extractAccessToken(request).orElse(null);
        boolean isAccessToken = false;
        if (accessToken != null) {
            try {
                isAccessToken = jwtTokenProvider.getTokenType(accessToken).equals("AccessToken");
            } catch (ExpiredJwtException e) {
                request.setAttribute("exception", "ExpiredJwtException"); //만료 에러.
                filterChain.doFilter(request, response);
                return;
            } catch (SecurityException | IllegalArgumentException | JwtException e) {
                request.setAttribute("exception", "AccessJwtException"); //유효하지 않은 예외.
                filterChain.doFilter(request, response);
                return;
            }


            if (isAccessToken == false) {
                request.setAttribute("exception", "AccessJwtException"); //엑세스 토큰이 아니기에 예외 반환.
            }
            else if(redisService.hasKeyBlackListToken(accessToken)){
                request.setAttribute("exception", "AccessJwtException"); //블랙리스트에 있는 토큰이기에 예외 반환.
            }
            else {
                String username = jwtTokenProvider.getUsername(accessToken);
                try {
                    UserDetails userDetails = principalDetailsService.loadUserByUsername(username);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());

                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                } catch (UsernameNotFoundException e) {
                    request.setAttribute("exception", "UsernameNotFoundException"); //유저 정보를 찾을 수 없다는 에러.

                }

            }
        }

        filterChain.doFilter(request, response);
    }
}
