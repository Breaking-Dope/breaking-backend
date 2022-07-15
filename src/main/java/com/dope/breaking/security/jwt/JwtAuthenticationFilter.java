package com.dope.breaking.security.jwt;

import com.dope.breaking.security.userDetails.PrincipalDetailsService;
import com.dope.breaking.service.RedisService;
import com.dope.breaking.service.UserService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {//모든 서블릿 컨테이너에서 요청 디스패치당 단일 실행을 보장하는 것을 목표로 하는 필터 기본 클래스
    private final JwtTokenProvider jwtTokenProvider;

    private final PrincipalDetailsService principalDetailsService;

    private final RedisService redisService;


    //인증작업을 실시함.
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if(request.getRequestURI().equals("/reissue")) {
            filterChain.doFilter(request, response); // GET: /reissue의 요청이라면 밑의 로직을 무시하고 Controller단까지 진입.
            return;
        }

        String accessToken = jwtTokenProvider.extractAccessToken(request).orElse(null);


        if (accessToken != null && jwtTokenProvider.validateToken(accessToken) == true) {

            String username = jwtTokenProvider.getUsername(accessToken);
            try {
                UserDetails userDetails = principalDetailsService.loadUserByUsername(username);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            }catch (UsernameNotFoundException e){
                log.info("유저 정보 없음");
                request.setAttribute("exception", "유저 정보를 찾을 수 없음");
            }
        } else if (accessToken != null && jwtTokenProvider.validateToken(accessToken) == false) {
            try {
                String username = jwtTokenProvider.getUsername(accessToken);
            } catch (ExpiredJwtException e) {
                log.info("Expiraion date");
                request.setAttribute("exception", "Access Token이 만료되었습니다.");
            } catch (SecurityException | IllegalArgumentException | JwtException e) {
                log.info("invalid sign");
                request.setAttribute("exception", "Access Token이 유효하지 않습니다.");
            }
        }


        filterChain.doFilter(request, response);
    }
}
