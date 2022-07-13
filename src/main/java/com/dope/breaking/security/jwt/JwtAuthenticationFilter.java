package com.dope.breaking.security.jwt;

import com.dope.breaking.security.userDetails.PrincipalDetailsService;
import com.dope.breaking.service.UserService;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {//모든 서블릿 컨테이너에서 요청 디스패치당 단일 실행을 보장하는 것을 목표로 하는 필터 기본 클래스
    private final JwtTokenProvider jwtTokenProvider;

    private final PrincipalDetailsService principalDetailsService;

    private final UserService userService;

    //인증작업을 실시함.
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        String accesstoken = jwtTokenProvider.extractAccessToken(request).orElse(null);
        String refreshtoken = jwtTokenProvider.extractRefreshToken(request).orElse(null);

        if (refreshtoken != null && jwtTokenProvider.validateToken(refreshtoken) == true) {
            log.info(String.valueOf(userService.findByRefreshToken(refreshtoken).isPresent()));
            if (userService.findByRefreshToken(refreshtoken).isPresent()) {

                String reissueAccessToken = jwtTokenProvider.createAccessToken(userService.findByRefreshToken(refreshtoken).get().getUsername());
                response.setStatus(HttpServletResponse.SC_OK);
                response.setHeader("Authorization", reissueAccessToken);
                Map<String, String> responseBody = new HashMap<>();
                responseBody.put("message", "Access Token 재발급이 완료되었습니다.");
                return;
            } else {
                request.setAttribute("exception", "Refresh Token이 유효하지 않습니다.");
            }
        } else if (accesstoken != null && jwtTokenProvider.validateToken(accesstoken) == true) {

            String username = jwtTokenProvider.getUsername(accesstoken);
            log.info(username);

            try {
                UserDetails userDetails = principalDetailsService.loadUserByUsername(username);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (UsernameNotFoundException e) {
                log.info("유저 정보 찾지 못함");
                request.setAttribute("exception", "유저 정보를 찾지 못했습니다.");
            }
        } else if (accesstoken != null && jwtTokenProvider.validateToken(accesstoken) == false) {
            try {
                String username = jwtTokenProvider.getUsername(accesstoken);
            }catch (ExpiredJwtException e) {
                log.info("Expiraion date");
                request.setAttribute("exception", "Access Token이 만료되었습니다.");
            }
            catch (SecurityException | IllegalArgumentException | JwtException e) {
                log.info("invalid sign");
                request.setAttribute("exception", "Access Token이 유효하지 않습니다.");
            }
        }
        filterChain.doFilter(request, response);
    }
}
