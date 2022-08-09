package com.dope.breaking.security.controller;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.security.jwt.JwtTokenProvider;
import com.dope.breaking.service.RedisService;
import com.dope.breaking.service.UserService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@Disabled
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Oauth2LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisService redisService;


    private final String accesstoken = "";
    private final String idtoken = "";
    private final String USERNAME = "";

    @DisplayName("유효한 엑스스토큰으로 구글 Oauth2 로그인을 시도 시, 정상적으로 유저 정보가 반환된다.")
    @Test
    void googleOauthLogin() throws Exception {

        Map<String, String> info = new LinkedHashMap<>();

        info.put("accessToken", accesstoken);
        info.put("idToken", idtoken);

        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/google")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "test"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.fullname").value("최현영[학생](소프트웨어융합대학 컴퓨터공학부)"))
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        String rescontent = response.getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(rescontent);
        ObjectNode objectNode = (ObjectNode) jsonNode;
        System.out.println(objectNode.toPrettyString());
    }

    @DisplayName("이미 가입한 유저가 유효한 엑스스토큰으로 구글 Oauth2 로그인을 시도 시, 예외를 반환한다.")
    @Test
    void googleOauthLoginWithSignUpUser() throws Exception {
        User user = User.builder()
                .username(USERNAME).build();

        userRepository.save(user);

        Map<String, String> info = new LinkedHashMap<>();
        info.put("accessToken", accesstoken);
        info.put("idToken", idtoken);
        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/google")
                        .header("User-Agent", "test")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();


        MockHttpServletResponse response = result.getResponse();
        System.out.println("accessToken : " + response.getHeaderValue("Authorization"));
        String accessjwt = (String) response.getHeaderValue("Authorization");
        System.out.println("refreshToken : " + response.getHeaderValue("Authorization-refresh"));
        String refreshjwt = (String) response.getHeaderValue("Authorization-refresh");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/google").header("Authorization","Bearer " + accessjwt)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        redisService.deleteValues("OTHER_104431269882060562135g");


    }


    @DisplayName("두개의 다른 플랫폼이 동시에 로그인을 시도하면, 각각 정상적으로 처리된다.")
    @Test
    void googleOauthLoginWithEachOtherPlatfom() throws Exception {
        User user = User.builder()
                .username(USERNAME).build();

        userRepository.save(user);

        Map<String, String> info = new LinkedHashMap<>();
        info.put("accessToken", accesstoken);
        info.put("idToken", idtoken);
        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result1 = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/google")
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.2; sdk Build/KK) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        Thread.sleep(1000);

        MvcResult result2 = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/google")
                        .header("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 11; SM-A908N Build/RP1A.200720.012)")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();


        MockHttpServletResponse response1 = result1.getResponse();
        System.out.println("accessToken : " + response1.getHeaderValue("Authorization"));
        String accessjwt1 = (String) response1.getHeaderValue("Authorization");
        System.out.println("refreshToken : " + response1.getHeaderValue("Authorization-refresh"));
        String refreshjwt1 = (String) response1.getHeaderValue("Authorization-refresh");

        MockHttpServletResponse response2 = result2.getResponse();
        System.out.println("accessToken : " + response2.getHeaderValue("Authorization"));
        String accessjwt2 = (String) response2.getHeaderValue("Authorization");
        System.out.println("refreshToken : " + response2.getHeaderValue("Authorization-refresh"));
        String refreshjwt2 = (String) response2.getHeaderValue("Authorization-refresh");

        Assertions.assertTrue(!accessjwt1.equals(accessjwt2));
        Assertions.assertTrue(!refreshjwt1.equals(refreshjwt2));
        Assertions.assertTrue(redisService.hasKey("WEB_104431269882060562135g"));
        Assertions.assertTrue(redisService.hasKey("ANDROID_104431269882060562135g"));

        redisService.deleteValues("ANDROID_104431269882060562135g");
        redisService.deleteValues("WEB_104431269882060562135g");
    }



}