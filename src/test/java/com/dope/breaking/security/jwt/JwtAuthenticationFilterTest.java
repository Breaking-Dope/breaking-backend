package com.dope.breaking.security.jwt;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.security.userDetails.PrincipalDetailsService;
import com.dope.breaking.service.UserService;
import com.dope.breaking.withMockCustomAuthorize.WithMockCustomUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assertions;
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
    private ObjectMapper objectMapper;

    private static String USERNAME = "";
    private static String PASSWORD = "123456789";

    static String accessjwt;
    static String refreshjwt;


    @DisplayName("AccessJwt and RefreshJwt issue")
    @Order(1)
    @Test
    void IssueJwtWithkakaoOauthLogin() throws Exception {
        userRepository.deleteAll();
        userRepository.save(User.builder().username(USERNAME).password(passwordEncoder.encode(PASSWORD)).role(Role.USER).build());


        String accesstoken = "";
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
        System.out.println("user refreshtoken : " + userRepository.findByRefreshToken(refreshjwt).get().getRefreshToken());
    }

    @DisplayName("No AccessJwt and RefreshJwt")
    @Order(2)
    @Test
    void noAccessAndRefreshToken() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/oauth2/validate-jwt"))//login??? ?????? ?????? ????????? ??????
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()); //???????????? ????????? 4xx ??????
    }

    @DisplayName("valid AccessJwt without RefreshJwt")
    @Order(3)
    @Test
    void validAccessWithoutRefreshToken() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/hello").header("Authorization", "Bearer " + accessjwt))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()); //????????? ????????? ??????????????? 2xx ??????
    }

    @DisplayName("invalid Accessjwt without RefreshJwt")
    @Order(4)
    @Test
    void invalidAccessWithoutRefreshToken() throws Exception{
        this.mockMvc.perform(MockMvcRequestBuilders.get("/hello").header("Authorization", "Bearer " + accessjwt + "123456"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()); //????????? ????????? ???????????? ????????? 4xx ??????.
    }



    @DisplayName("valid RefreshJwt without AccessJwt")
    @Order(5)
    @Test
    void validRefreshWithoutAccessToken() throws Exception{

        System.out.println(refreshjwt);
        System.out.println(userRepository.findByUsername(USERNAME).get().getRefreshToken());


        MvcResult result  = this.mockMvc.perform(MockMvcRequestBuilders.get("/hello").header("Authorization-refresh", "Bearer " + refreshjwt))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn(); //??????????????? ??????.

        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        String reaccessjwt = (String) response.getHeaderValue("Authorization");
        System.out.println(reaccessjwt);
    }


    @DisplayName("invalid RefershJwt without AccessToken") //???????????? ?????? refreshtoken?????? ???????????? 4xx ??????
    @Order(6)
    @Test
    void invalidRefreshWithoutAccessToken() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/hello").header("Authorization-refresh", "Bearer " + refreshjwt+ "1214"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }


    @DisplayName("valid AccessJwt and RefreshJwt")
    @Order(7)
    @Test
    void validAccessAndRefreshToken() throws Exception{
        MvcResult result  = this.mockMvc.perform(MockMvcRequestBuilders.get("/hello")
                        .header("Authorization", "Bearer " + accessjwt) //accessjwt
                        .header("Authorization-refresh", "Bearer " + refreshjwt))//refershjwt
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn(); //??????????????? ??????.

        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        System.out.println("refershToken : " + response.getHeaderValue("Authorization-refresh"));
        String reaccessjwt = (String) response.getHeaderValue("Authorization");
        String rerefreshjwt = (String) response.getHeaderValue("Authorization-refresh");
        Assertions.assertThat(rerefreshjwt).isNull();
        System.out.println(reaccessjwt);
    }

    @DisplayName("invalid  AccessJwt but valid RefreshJwt") //???????????? ?????? ????????? ??????????????? ????????? ????????? ????????? ????????? refresh ????????? ?????? ????????? ?????????.
    @Order(8)
    @Test
    void invalidAccessButValidRefresh() throws Exception{
        MvcResult result  = this.mockMvc.perform(MockMvcRequestBuilders.get("/hello")
                        .header("Authorization", "Bearer " + accessjwt+ "2134") //accessjwt
                        .header("Authorization-refresh", "Bearer " + refreshjwt))//refershjwt
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn(); //refresh ????????? ??????????????? ????????????.

        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        System.out.println("refershToken : " + response.getHeaderValue("Authorization-refresh"));
        String reaccessjwt = (String) response.getHeaderValue("Authorization");
        String rerefreshjwt = (String) response.getHeaderValue("Authorization-refresh");
        Assertions.assertThat(reaccessjwt).isNotEmpty();
        Assertions.assertThat(rerefreshjwt).isNull();
        System.out.println(reaccessjwt);

    }



    @DisplayName("Valid AccessJwt but Invalid RefreshJwt")
    @Order(9)
    @Test
    void validAccessButinvalidRefresh() throws Exception{
        this.mockMvc.perform(MockMvcRequestBuilders.get("/hello")
                        .header("Authorization", "Bearer " + accessjwt)
                        .header("Authorization-refresh", "Bearer " + refreshjwt+ "1214"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }


    @DisplayName("Invalid AccessJwt and RefershJwt")
    @Order(10)
    @Test
    void invalidAccessAndRefresh() throws Exception{
        MvcResult result  = this.mockMvc.perform(MockMvcRequestBuilders.get("/hello")
                        .header("Authorization", "Bearer " + accessjwt+ "2134") //accessjwt
                        .header("Authorization-refresh", "Bearer " + refreshjwt + "1245"))//refershjwt
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn(); //?????? ????????? ??????????????? 4xx

        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        System.out.println("refershToken : " + response.getHeaderValue("Authorization-refresh"));
        String reaccessjwt = (String) response.getHeaderValue("Authorization");
        String rerefreshjwt = (String) response.getHeaderValue("Authorization-refresh");
        Assertions.assertThat(reaccessjwt).isNull();
        Assertions.assertThat(rerefreshjwt).isNull();
    }




}