package com.dope.breaking.security.controller;

import com.dope.breaking.security.jwt.JwtTokenProvider;
import com.dope.breaking.service.UserService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.Assertions;
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
        String accesstoken = "ya29.A0AVA9y1uxYysIWVXbntvBZMNkYZnWdDaYo4xZCZwZP_msI7sDQlu_TygXhrUm5nccF8wlLSFBBJuiSNIojztNc88h5HwDuiSuNO6I2_OvTr7opR28-RJkeHZBSlDIQgqQFmWG3UjfpVoY2lmcv4JG3ne51GjYtAYUNnWUtBVEFTQVRBU0ZRRTY1ZHI4cjVTR0M2ek5EWVBSOU53Tkh1S1UzQQ0165";
        String idtoken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjFiZDY4NWY1ZThmYzYyZDc1ODcwNWMxZWIwZThhNzUyNGM0NzU5NzUiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI3MzY0NTMzMjUxMzItYjNqcmVydTJzaTQydTltYnFudjBuOWVxdmNucDQxMDkuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI3MzY0NTMzMjUxMzItYjNqcmVydTJzaTQydTltYnFudjBuOWVxdmNucDQxMDkuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDQ0MzEyNjk4ODIwNjA1NjIxMzUiLCJoZCI6ImtodS5hYy5rciIsImVtYWlsIjoiY2h5MDMxMEBraHUuYWMua3IiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6Im5UQWF3d292azZWc0F4SXRCRWlYUGciLCJuYW1lIjoi4oCN7LWc7ZiE7JiBW-2VmeyDnV0o7IaM7ZSE7Yq47Juo7Ja07Jy17ZWp64yA7ZWZIOy7tO2TqO2EsOqzte2Vmeu2gCkiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUl0YnZtbUp0RTBrMXo1T2hUMm5OcHpsZjV6anBiUm0yVWJOeEliaDQwcWw9czk2LWMiLCJnaXZlbl9uYW1lIjoi7LWc7ZiE7JiBW-2VmeyDnV0o7IaM7ZSE7Yq47Juo7Ja07Jy17ZWp64yA7ZWZIOy7tO2TqO2EsOqzte2Vmeu2gCkiLCJmYW1pbHlfbmFtZSI6IuKAjSIsImxvY2FsZSI6ImtvIiwiaWF0IjoxNjU3NTM0NzU0LCJleHAiOjE2NTc1MzgzNTR9.kA6z_ABgsiai-MMQ_rUDiTajsas9RR3Vac57G4w9BKPMmRP8KxjAXhO-3I9mQ6lsYVB3NkbztS2dBg1d1mDdHd7qgLxkpE358KmmaYzmTEu971GZ6sN35N-1KSgszkQ92wM-pibgYGXlfI4xxMXYaotU8jWuVT7YcnqSYc0mHsR_iK4cK7q6f11kzUHSvArln-QQB6Ea7QgSX9f5SxAPoKtx_Uj3tbScVJTrfjW6AqcOsxx-jxaJ_EJQJGaAzWmnOYqKPquKk4d9fv7cnWGD75G8EdZG9_j9KrHQ3_0RN-LA_C7g092DU9yx8Vjly86dT1uC19trKVWdtfPBf_SQbg";

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
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("104431269882060562135g"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("chy0310@khu.ac.kr"))
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        String rescontent = response.getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(rescontent);
        ObjectNode objectNode = (ObjectNode) jsonNode;
        System.out.println(objectNode.toPrettyString());


    }


}