package com.dope.breaking.Security.config;

import com.dope.breaking.Security.Handler.JwtExceptHandler;
import com.dope.breaking.Security.Jwt.JwtAuthenticationFilter;
import com.dope.breaking.Security.Jwt.JwtEntryPoint;
import com.dope.breaking.Security.Jwt.JwtTokenProvider;
import com.dope.breaking.Security.UserDetails.PrincipalDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Slf4j
@RequiredArgsConstructor
@Configuration //빈으로 등록
@EnableGlobalMethodSecurity(prePostEnabled = true)//로그인인증을 거친자와 안거친 자를 구별할 수 있도록 함.
@EnableWebSecurity //스프링 시큐리티를 활성화하겠다.
public class WebSecurityconfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;

    private final PrincipalDetailsService principalDetailsService;

    private final UserRepository userRepository;



    private final PasswordEncoder passwordEncoder;


    @Bean
    public AuthenticationManager authenticationManager() {//AuthenticationManager 등록
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();//Form 형식의 DaoAuthenticationProvider 사용 -> 여기서 아이디와 password를 대조하여 비교함.
        provider.setPasswordEncoder(passwordEncoder);//PasswordEncoder로는 BCry를 사용할 것임.
        provider.setUserDetailsService(principalDetailsService); //UserDtailsService는 예전에 작성한 것으로. 유저 인증절차는 이친구에게 넘김.
        return new ProviderManager(provider);
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().disable() //rest api만 고려하면 해제해도 되는듯하다.
                .csrf().disable() //csrf() 설정은 로컬환경이므로 필요가 없다.
                .headers()
                .addHeaderWriter(new XFrameOptionsHeaderWriter( //h2 콘솔을사용하기 위해
                        XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) //토큰 기반 인증이라 세션은 사용하지 않음
                .and()
                .formLogin().disable() // Restapi이므로 form 로그인은 필요가 없다.
                .authorizeRequests() //요청에 대한 권한 체크
                .mvcMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Preflight Request 허용해주기 ->CORS 정책
                .antMatchers("/press/**").hasRole("PRESS") //PRESS를 가진 권한만이 접근이 허용됨
                .antMatchers("/user/**").hasRole("USER") //USER를 가진 권한만이 접근이 허용됨
                .antMatchers("/**").permitAll() //그외 접근은 모두 허용됨.
                .and()
                .exceptionHandling().authenticationEntryPoint(jwtEnrtyPoint()); //에러코드 반환할 except


        http.addFilterBefore(jwtAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class); //JwtAuthenticationFilter를 JsonUsernamePasswordAuthenticationFilter 전에 넣음
        http.addFilterBefore(jwtExceptHandler(), JwtAuthenticationFilter.class); //예외 처리를 위해 두자.

    }

    @Bean
    JwtExceptHandler jwtExceptHandler() {
        JwtExceptHandler jwtExcept = new JwtExceptHandler();
        return jwtExcept;
    }

    @Bean
    JwtEntryPoint jwtEnrtyPoint() {
        JwtEntryPoint jwtEnrtyPoint = new JwtEntryPoint();
        return jwtEnrtyPoint;
    }


    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider, principalDetailsService);
        return jwtAuthenticationFilter;
    }
}
