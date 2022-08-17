package com.dope.breaking.security.controller;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.security.jwt.JwtTokenProvider;
import com.dope.breaking.service.RedisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;


@Disabled
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Oauth2LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisService redisService;

    private final String GOOGLE_ACCESSTOKEN = "";
    private final String GOOGLE_ID_TOKEN = "";
    private final String GOOGLE_USERNAME = "";

    private final String KAKAO_ACCESSTOKEN = "";

    private final String KAKAO_USERNAME = "";

    @DisplayName("유효한 엑스스토큰으로 구글 Oauth2 로그인을 시도 시, 정상적으로 유저 정보가 반환된다.")
    @Test
    void googleOauthLogin() throws Exception {

        Map<String, String> info = new LinkedHashMap<>();

        info.put("accessToken", GOOGLE_ACCESSTOKEN);
        info.put("idToken", GOOGLE_ID_TOKEN);

        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/google")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "test"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        String rescontent = response.getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(rescontent);
        ObjectNode objectNode = (ObjectNode) jsonNode;
        System.out.println(objectNode.toPrettyString());
    }
    @DisplayName("유효하지 않은 엑세스 토큰으로 구글 로그인을 시도 할 시, 예외를 반환한다.")
    @Test
    void googleOauthLoginWithSignUpUser() throws Exception {
        User user = User.builder()
                .username(GOOGLE_USERNAME).build();

        userRepository.save(user);

        Map<String, String> info = new LinkedHashMap<>();
        info.put("accessToken", GOOGLE_ACCESSTOKEN + 1234);
        info.put("idToken", GOOGLE_ID_TOKEN);
        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/google")
                        .header("User-Agent", "test")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }


    @DisplayName("두개의 다른 플랫폼이 동시에 구글 로그인을 시도하면, 각각 정상적으로 처리된다.")
    @Test
    void googleOauthLoginWithEachOtherPlatfom() throws Exception {
        User user = User.builder()
                .username(GOOGLE_USERNAME).build();

        userRepository.save(user);

        Map<String, String> info = new LinkedHashMap<>();
        info.put("accessToken", GOOGLE_ACCESSTOKEN);
        info.put("idToken", GOOGLE_ID_TOKEN);
        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result1 = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/google")
                        .header("User-Agent", "PostmanRuntime")
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
        Assertions.assertTrue(redisService.hasKey("POSTMAN_"+ GOOGLE_USERNAME));
        Assertions.assertTrue(redisService.hasKey("ANDROID_"+ GOOGLE_USERNAME));

        redisService.deleteValues("ANDROID_"+ GOOGLE_USERNAME);
        redisService.deleteValues("POSTMAN_"+ GOOGLE_USERNAME);
    }




    @DisplayName("유효한 엑스스토큰으로 카카오 Oauth2 로그인을 시도 시, 정상적으로 유저 정보가 반환된다.")
    @Test
    void kakaoOauthLogin() throws Exception {

        Map<String, String> info = new LinkedHashMap<>();

        info.put("accessToken", KAKAO_ACCESSTOKEN);

        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/kakao")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "test"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        String rescontent = response.getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(rescontent);
        ObjectNode objectNode = (ObjectNode) jsonNode;
        System.out.println(objectNode.toPrettyString());
    }

    @DisplayName("유효하지 않은 엑세스 토큰으로 구글 로그인을 시도 할 시, 예외를 반환한다.")
    @Test
    void kakaoOauthLoginWithSignUpUser() throws Exception {
        User user = User.builder()
                .username(KAKAO_USERNAME).build();

        userRepository.save(user);

        Map<String, String> info = new LinkedHashMap<>();
        info.put("accessToken", KAKAO_ACCESSTOKEN);
        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/kakao")
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

    }


    @DisplayName("두개의 다른 플랫폼이 동시에 카카오 로그인을 시도하면, 각각 정상적으로 처리된다.")
    @Test
    void kakaoOauthLoginWithEachOtherPlatfom() throws Exception {
        User user = User.builder()
                .username(KAKAO_USERNAME).build();

        userRepository.save(user);

        Map<String, String> info = new LinkedHashMap<>();
        info.put("accessToken", KAKAO_ACCESSTOKEN);
        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result1 = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/kakao")
                        .header("User-Agent", "PostmanRuntime")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        Thread.sleep(1000);

        MvcResult result2 = this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/oauth2/sign-in/kakao")
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
        Assertions.assertTrue(redisService.hasKey("POSTMAN_"+ KAKAO_USERNAME));
        Assertions.assertTrue(redisService.hasKey("ANDROID_"+ KAKAO_USERNAME));

        redisService.deleteValues("ANDROID_"+ KAKAO_USERNAME);
        redisService.deleteValues("POSTMAN_"+ KAKAO_USERNAME);
    }



}