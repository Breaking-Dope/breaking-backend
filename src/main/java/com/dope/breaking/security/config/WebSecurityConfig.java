package com.dope.breaking.security.config;


import com.dope.breaking.security.jwt.JwtAuthenticationFilter;
import com.dope.breaking.security.jwt.JwtEntryPoint;
import com.dope.breaking.security.jwt.JwtTokenProvider;
import com.dope.breaking.security.userDetails.PrincipalDetailsService;
import com.dope.breaking.service.RedisService;
import com.dope.breaking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration //빈으로 등록
@EnableGlobalMethodSecurity(prePostEnabled = true)//로그인인증을 거친자와 안거친 자를 구별할 수 있도록 함.
@EnableWebSecurity //스프링 시큐리티를 활성화하겠다.
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;

    private final PrincipalDetailsService principalDetailsService;

    private final PasswordEncoder passwordEncoder;

    private final RedisService redisService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .httpBasic().disable() //rest api만 고려하면 해제해도 되는듯하다.
            .csrf().disable()
            .cors()
            .configurationSource(corsConfigurationSource())
            .and()
                .headers().frameOptions().disable()
            .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) //토큰 기반 인증이라 세션은 사용하지 않음
            .and()
                .formLogin().disable() // Restapi이므로 form 로그인은 필요가 없다.
                .httpBasic().disable()
                .authorizeRequests() //요청에 대한 권한 체크
                .mvcMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Preflight Request 허용해주기 ->CORS 정책
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                .antMatchers("/press/**").hasRole("PRESS") //PRESS를 가진 권한만이 접근이 허용됨
                .antMatchers("/user/**").hasRole("USER") //USER를 가진 권한만이 접근이 허용됨
                .antMatchers("/**").permitAll() //그외 접근은 모두 허용됨.
            .and()
                .exceptionHandling().authenticationEntryPoint(jwtEntryPoint()); //에러코드 반환할 ExceptionPoint

        http.addFilterBefore(jwtAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class); //JwtAuthenticationFilter를 JsonUsernamePasswordAuthenticationFilter 전에 넣음

    }

    @Bean
    JwtEntryPoint jwtEntryPoint() {
        JwtEntryPoint jwtEntryPoint = new JwtEntryPoint();
        return jwtEntryPoint;
    }


    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider, principalDetailsService, redisService);
        return jwtAuthenticationFilter;
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://team-dope.link:3000", "https://team-dope.link"));

        configuration.addAllowedMethod("*");

        configuration.addAllowedHeader("authorization");
        configuration.addAllowedHeader("authorization-refresh");
        configuration.addAllowedHeader("User-Agent");
        configuration.addAllowedHeader("Content-Type");

        configuration.addExposedHeader("authorization");
        configuration.addExposedHeader("authorization-refresh");
        configuration.addExposedHeader("User-Agent");
        configuration.addExposedHeader("Content-Type");

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
