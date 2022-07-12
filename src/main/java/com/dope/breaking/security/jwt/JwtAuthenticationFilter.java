package com.dope.breaking.security.jwt;

import com.dope.breaking.security.userDetails.PrincipalDetailsService;
import com.dope.breaking.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

        String accesstoken = jwtTokenProvider.extractAccessToken(request).orElse(null); //accesstoken으로 받아졌는지 확인
        log.info("accessToken :  {}" , accesstoken );
        String refreshtoken = jwtTokenProvider.extractRefreshToken(request).orElse(null); //refreshtoken으로 받야졌는지 확인
        log.info("refreshToken : {}", refreshtoken);


        //엑세스 토큰은 null이 아니고 엑세스 토큰이 유효하다면

        /*
        refresh토큰이 우선적으로 유효한지를 살펴보아야 한다고 생각함. 그리고 유효하다면? 재발행한다. 엑세스 토큰만 주는 경우는 접근하기 위함이고, refresh토큰을 지녔다는 것은 재발행 요청을 했다는 뜻. 둘다 보내도, 리플리쉬 토큰을 보냈음으로 재발행한다.
         */
        if(refreshtoken != null && jwtTokenProvider.validateToken(refreshtoken) == true){
            log.info(String.valueOf(userService.findByRefreshToken(refreshtoken).isPresent()));
            if(userService.findByRefreshToken(refreshtoken).isPresent()){

                String reissueAccessToken = jwtTokenProvider.createAccessToken(userService.findByRefreshToken(refreshtoken).get().getUsername());
                response.setStatus(HttpServletResponse.SC_OK);
                response.setHeader("Authorization", reissueAccessToken);
                Map<String, String> responseBody = new HashMap<>();
                responseBody.put("message", "AccessToken 재발급 완료");
                return; //바로 반환한다.
            }
            else{
                request.setAttribute("exception", "RefreshToken is invalid");
            }
        }
        else if (accesstoken != null && jwtTokenProvider.validateToken(accesstoken) == true) {

            String username = jwtTokenProvider.getUsername(accesstoken);
            log.info(username);

            try {
                UserDetails userDetails = principalDetailsService.loadUserByUsername(username);
                log.info("Userdetail : {}", userDetails.getUsername());
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());//비밀번호는 인증단계에서는 필요없으므로,

                SecurityContext context = SecurityContextHolder.createEmptyContext(); //컨텍스트 텅 비어있는 컨텍스트 객체 생성
                context.setAuthentication(authentication);//SecurityContext에 Authentication 객체를 저장
                SecurityContextHolder.setContext(context); //contextholder에 authentication 객체를 저장한 컨텍스트를 담게함.
            }
            catch(Exception e){
                log.info("유저 정보 찾지 못함");
                request.setAttribute("exception", "User Not Found");
            }
            //스프링 시큐리티가 수행해주는 권한 처리를 위해 아래와 같이 토큰을 만들어서 Authentication 객체를 강제로 만들고 컨텍스트에 저장한다.

            //다음 체인필터로 이동
        }
        else if(accesstoken != null && jwtTokenProvider.validateToken(accesstoken) == false){
            try {
                String username = jwtTokenProvider.getUsername(accesstoken); //해독 과정 중 에러가 발생함.
            } catch (SecurityException | MalformedJwtException | IllegalArgumentException e) {
                request.setAttribute("exception", "Invalid Signature");
            } catch (ExpiredJwtException e) {
                request.setAttribute("exception", "Expiration date");
            } catch (Exception e) {
                request.setAttribute("exception", "Other errors related to jwt");
            }
        }
        filterChain.doFilter(request, response);
    }
}
