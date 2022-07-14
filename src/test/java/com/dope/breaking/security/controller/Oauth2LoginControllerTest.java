package com.dope.breaking.security.controller;

import com.dope.breaking.security.jwt.JwtTokenProvider;
import com.dope.breaking.service.UserService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@Disabled
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class Oauth2LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;



    @DisplayName("구글 Oauth2 로그인")
    @Test
    void googleOauthLogin() throws Exception {
        String accesstoken = ""; String idtoken = "";
        Map<String, String> info = new LinkedHashMap<>();

        info.put("accessToken", accesstoken);
        info.put("idToken", idtoken);

        String content = objectMapper.writeValueAsString(info);
        System.out.println(content);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                .post("/oauth2/sign-in/google")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.fullname").value("최현영[학생](소프트웨어융합대학 컴퓨터공학부)"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(""))
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        String rescontent = response.getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(rescontent);
        ObjectNode objectNode = (ObjectNode) jsonNode;
        System.out.println(objectNode.toPrettyString());


    }


}