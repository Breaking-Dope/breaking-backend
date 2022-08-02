package com.dope.breaking.security.jwt;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.security.userDetails.PrincipalDetailsService;
import com.dope.breaking.service.RedisService;
import com.dope.breaking.service.UserService;
import com.dope.breaking.withMockCustomAuthorize.WithMockCustomUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assertions;
import org.hibernate.annotations.Target;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;


@Disabled
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationFilterTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    PrincipalDetailsService principalDetailsService;

    @Autowired
    RedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;

    private static String USERNAME = "";
    private static String PASSWORD = "123456789";

    private static String accesstoken = "";

    static String accessjwt;
    static String refreshjwt;


    @Transactional
    @DisplayName("엑시스 토큰과 리플리스 토큰이 정상적으로 발급된다.")
    @Test
    void IssueJwtWithkakaoOauthLogin() throws Exception {
        userRepository.deleteAll();
        userRepository.save(User.builder().username(USERNAME).password(passwordEncoder.encode(PASSWORD)).role(Role.USER).build());



        Map<String, String> info = new LinkedHashMap<>();

        info.put("accessToken", accesstoken);

        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/kakao")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        accessjwt = (String) response.getHeaderValue("Authorization");
        System.out.println("refreshToken : " + response.getHeaderValue("Authorization-refresh"));
        refreshjwt = (String) response.getHeaderValue("Authorization-refresh");
        System.out.println("user refreshtoken : " + redisService.getData(USERNAME));

        redisService.deleteValues(USERNAME);
    }

    @DisplayName("엑세스 토큰과 리플리쉬 토큰 없이 인증된 유저만 접근 가능한 주소로 접근시 예외가 반환된다.")
    @Transactional
    @Test
    void noAccessAndRefreshToken() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/hello"))//login이 아닌 다른 임의의 주소
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()); //아무것도 없다면 4xx 반환
    }

    @DisplayName("유효한 엑세스 토큰만으로 인증된 유저만 접근 가능한 주소로 접근 시 정상적으로 요청이 완료된다.")
    @Transactional
    @Test
    void validAccessWithoutRefreshToken() throws Exception {
        //Given
        userRepository.save(User.builder().username(USERNAME).password(passwordEncoder.encode(PASSWORD)).role(Role.USER).build());


        Map<String, String> info = new LinkedHashMap<>();

        info.put("accessToken", accesstoken);

        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/kakao")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        accessjwt = (String) response.getHeaderValue("Authorization");
        System.out.println("refreshToken : " + response.getHeaderValue("Authorization-refresh"));



        //When
        this.mockMvc.perform(MockMvcRequestBuilders.get("/hello").header("Authorization", "Bearer " + accessjwt))
                .andDo(MockMvcResultHandlers.print())
                //Then
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()); //엑세스 토큰이 유효하다면 2xx 반환 /


        redisService.deleteValues(USERNAME);
    }

    @DisplayName("유효하지 않은 엑세스 토큰만으로 인증된 유저만 접근 가능한 주소로 접근 시, 예외가 반환된다.")
    @Transactional
    @Test
    void invalidAccessWithoutRefreshToken() throws Exception{
        //Given
        userRepository.save(User.builder().username(USERNAME).password(passwordEncoder.encode(PASSWORD)).role(Role.USER).build());


        Map<String, String> info = new LinkedHashMap<>();

        info.put("accessToken", accesstoken);

        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/kakao")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        accessjwt = (String) response.getHeaderValue("Authorization");
        System.out.println("refreshToken : " + response.getHeaderValue("Authorization-refresh"));
        //When
        this.mockMvc.perform(MockMvcRequestBuilders.get("/hello").header("Authorization", "Bearer " + accessjwt + "123456"))
                .andDo(MockMvcResultHandlers.print())
                //Then
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()); //엑세스 토큰이 유효하지 않다면 4xx 반환.


        redisService.deleteValues(USERNAME);
    }



    @DisplayName("엑세스 토큰만으로 인증된 유저만 접근 가능한 주소로 접근 시 정상적으로 요청이 완료된다.")
    @Transactional
    @Test
    void validRefreshWithoutAccessToken() throws Exception{
        userRepository.save(User.builder().username(USERNAME).password(passwordEncoder.encode(PASSWORD)).role(Role.USER).build());


        Map<String, String> info = new LinkedHashMap<>();

        info.put("accessToken", accesstoken);

        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/kakao")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        accessjwt = (String) response.getHeaderValue("Authorization");
        System.out.println("refreshToken : " + response.getHeaderValue("Authorization-refresh"));
        refreshjwt = (String) response.getHeaderValue("Authorization-refresh");
        System.out.println("user refreshtoken : " + redisService.getData(refreshjwt));

        result  = this.mockMvc.perform(MockMvcRequestBuilders.get("/hello").header("Authorization-refresh", "Bearer " + refreshjwt))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();

        response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        String reaccessjwt = (String) response.getHeaderValue("Authorization");
        System.out.println(reaccessjwt);


        redisService.deleteValues(USERNAME);
    }



    @DisplayName("유효한 엑세스 토큰과 리플리쉬 토큰으로 인증된 유저만 접근 가능한 주소로 접근 시 정상적으로 요청이 완료된다.")
    @Transactional
    @Test
    void validAccessAndRefreshToken() throws Exception{
        userRepository.save(User.builder().username(USERNAME).password(passwordEncoder.encode(PASSWORD)).role(Role.USER).build());


        Map<String, String> info = new LinkedHashMap<>();

        info.put("accessToken", accesstoken);

        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/kakao")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        accessjwt = (String) response.getHeaderValue("Authorization");
        System.out.println("refreshToken : " + response.getHeaderValue("Authorization-refresh"));
        refreshjwt = (String) response.getHeaderValue("Authorization-refresh");
        System.out.println("user refreshtoken : " + redisService.getData(refreshjwt));


        result  = this.mockMvc.perform(MockMvcRequestBuilders.get("/hello")
                        .header("Authorization", "Bearer " + accessjwt) //accessjwt
                        .header("Authorization-Refresh", "Bearer " + refreshjwt))//refershjwt
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn(); //재발급이기 때문.

        response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        System.out.println("refreshToken : " + response.getHeaderValue("Authorization-refresh"));
        String reaccessjwt = (String) response.getHeaderValue("Authorization");
        String rerefreshjwt = (String) response.getHeaderValue("Authorization-refresh");
        Assertions.assertThat(rerefreshjwt).isNull();
        System.out.println(reaccessjwt);


        redisService.deleteValues(USERNAME);
    }

    @DisplayName("유효하지 않은 엑세스토큰이지만 유효한 리플리쉬 토큰으로 인증된 유저만 접근가능한 주소로 접근 시 예외를 반환한다.") //유효기간 또는 잘못된 시그니처를 포함한 엑세스 토큰과 올바른 refresh 토큰이 같이 왔다면 재발행.
    @Transactional
    @Test
    void invalidAccessButValidRefresh() throws Exception{
        userRepository.save(User.builder().username(USERNAME).password(passwordEncoder.encode(PASSWORD)).role(Role.USER).build());


        Map<String, String> info = new LinkedHashMap<>();

        info.put("accessToken", accesstoken);

        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/kakao")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        accessjwt = (String) response.getHeaderValue("Authorization");
        System.out.println("refreshToken : " + response.getHeaderValue("Authorization-refresh"));
        refreshjwt = (String) response.getHeaderValue("Authorization-refresh");
        System.out.println("user refreshtoken : " + redisService.getData(refreshjwt));

        result  = this.mockMvc.perform(MockMvcRequestBuilders.get("/hello")
                        .header("Authorization", "Bearer " + accessjwt+ "2134") //accessjwt
                        .header("Authorization-Refresh", "Bearer " + refreshjwt))//refershjwt
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();

        redisService.deleteValues(USERNAME);

    }



    @DisplayName("유효한 엑세스 토큰이지만 유효하지 않은 리플리쉬 토큰으로 인증된 유저만 접근 가능한 주소로 접근 시 정상적으로 요청이 완료된다.")
    @Transactional
    @Test
    void validAccessButinvalidRefresh() throws Exception{
        userRepository.save(User.builder().username(USERNAME).password(passwordEncoder.encode(PASSWORD)).role(Role.USER).build());


        Map<String, String> info = new LinkedHashMap<>();

        info.put("accessToken", accesstoken);

        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/kakao")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        accessjwt = (String) response.getHeaderValue("Authorization");
        System.out.println("refreshToken : " + response.getHeaderValue("Authorization-refresh"));
        refreshjwt = (String) response.getHeaderValue("Authorization-refresh");
        System.out.println("user refreshtoken : " + redisService.getData(refreshjwt));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/hello")
                        .header("Authorization", "Bearer " + accessjwt)
                        .header("Authorization-Refresh", "Bearer " + refreshjwt+ "1214"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());


        redisService.deleteValues(USERNAME);
    }


    @DisplayName("유효하지 않은 엑세스 토큰과 리플리쉬 토큰으로 인증된 유저만 접근 가능한 주소로 접근 시, 예외가 반환된다.")
    @Transactional
    @Test
    void invalidAccessAndRefresh() throws Exception{

        userRepository.save(User.builder().username(USERNAME).password(passwordEncoder.encode(PASSWORD)).role(Role.USER).build());

        Map<String, String> info = new LinkedHashMap<>();

        info.put("accessToken", accesstoken);

        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/kakao")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        accessjwt = (String) response.getHeaderValue("Authorization");
        System.out.println("refreshToken : " + response.getHeaderValue("Authorization-refresh"));
        refreshjwt = (String) response.getHeaderValue("Authorization-refresh");
        System.out.println("user refreshtoken : " + redisService.getData(refreshjwt));



        result  = this.mockMvc.perform(MockMvcRequestBuilders.get("/hello")
                        .header("Authorization", "Bearer " + accessjwt+ "2134") //accessjwt
                        .header("Authorization-Refresh", "Bearer " + refreshjwt + "1245"))//refershjwt
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn(); //모두 잘못된 토큰이기에 4xx

         response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        System.out.println("refreshToken : " + response.getHeaderValue("Authorization-refresh"));
        String reaccessjwt = (String) response.getHeaderValue("Authorization");
        String rerefreshjwt = (String) response.getHeaderValue("Authorization-refresh");
        Assertions.assertThat(reaccessjwt).isNull();
        Assertions.assertThat(rerefreshjwt).isNull();


        redisService.deleteValues(USERNAME);
    }


    @DisplayName("유효한 엑세스 토큰과 리플리쉬 토큰으로 재발행을 요청하면 정상적으로 재발행된다.")
    @Transactional
    @Test
    void reissueWithValidTokens() throws Exception {
        userRepository.save(User.builder().username(USERNAME).password(passwordEncoder.encode(PASSWORD)).role(Role.USER).build());

        Map<String, String> info = new LinkedHashMap<>();

        info.put("accessToken", accesstoken);

        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/kakao")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        accessjwt = (String) response.getHeaderValue("Authorization");
        System.out.println("refreshToken : " + response.getHeaderValue("Authorization-refresh"));
        refreshjwt = (String) response.getHeaderValue("Authorization-refresh");
        System.out.println("user refreshtoken : " + redisService.getData(refreshjwt));

         result = this.mockMvc.perform(MockMvcRequestBuilders.get("/reissue")
                        .header("Authorization", "Bearer " + accessjwt) //accessjwt
                        .header("Authorization-Refresh", "Bearer " + refreshjwt))//refershjwt
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        System.out.println("refreshToken : " + response.getHeaderValue("Authorization-Refresh"));
        String reaccessjwt = (String) response.getHeaderValue("Authorization");
        String rerefreshjwt = (String) response.getHeaderValue("Authorization-refresh");
        Assertions.assertThat(reaccessjwt).isNotNull();
        Assertions.assertThat(rerefreshjwt).isNotNull();


        redisService.deleteValues(USERNAME);

    }

    @DisplayName("유효한 엑세스 토큰이지만 유효하지 않은 리플리쉬 토큰으로 재발행을 요청하면, 예외를 반환한다.")
    @Transactional
    @Test
    void reissueWithvalidAccessButInvalidRefresh() throws Exception {

        userRepository.save(User.builder().username(USERNAME).password(passwordEncoder.encode(PASSWORD)).role(Role.USER).build());


        Map<String, String> info = new LinkedHashMap<>();

        info.put("accessToken", accesstoken);

        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/kakao")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        accessjwt = (String) response.getHeaderValue("Authorization");
        System.out.println("refreshToken : " + response.getHeaderValue("Authorization-refresh"));
        refreshjwt = (String) response.getHeaderValue("Authorization-refresh");
        System.out.println("user refreshtoken : " + redisService.getData(refreshjwt));

         result = this.mockMvc.perform(MockMvcRequestBuilders.get("/reissue")
                        .header("Authorization", "Bearer " + accessjwt) //accessjwt
                        .header("Authorization-Refresh", "Bearer " + refreshjwt + "dsafsaf"))//refershjwt
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();


        redisService.deleteValues(USERNAME);

    }

    @DisplayName("유효하지 않은 엑세스 토큰과 유효한 리플리시 토큰으로 재발행을 요청하면 정상적으로 재발행된다.")
    @Transactional
    @Test
    void reissueWithInvalidAccessButValidRefrsh() throws Exception {
        userRepository.save(User.builder().username(USERNAME).password(passwordEncoder.encode(PASSWORD)).role(Role.USER).build());

        Map<String, String> info = new LinkedHashMap<>();

        info.put("accessToken", accesstoken);

        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/kakao")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        accessjwt = (String) response.getHeaderValue("Authorization");
        System.out.println("refreshToken : " + response.getHeaderValue("Authorization-refresh"));
        refreshjwt = (String) response.getHeaderValue("Authorization-refresh");
        System.out.println("user refreshtoken : " + redisService.getData(refreshjwt));



        Thread.sleep(1000);

        result = this.mockMvc.perform(MockMvcRequestBuilders.get("/reissue")
                        .header("Authorization", "Bearer " + accessjwt+"123") //accessjwt
                        .header("Authorization-Refresh", "Bearer " + refreshjwt))//refershjwt
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();



        redisService.deleteValues(USERNAME);
    }



}