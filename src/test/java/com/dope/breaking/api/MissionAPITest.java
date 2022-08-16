package com.dope.breaking.api;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.mission.MissionRequestDto;
import com.dope.breaking.dto.post.LocationDto;
import com.dope.breaking.repository.MissionRepository;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.withMockCustomAuthorize.WithMockCustomUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class MissionAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @DisplayName("언론사가 브레이킹 미션을 게시할 경우, 미션이 정상적으로 게시 된다.")
    @WithMockCustomUser
    @Transactional
    @Test
    void createMissionByPress() throws Exception {

        //Given
        User user = new User("12345g",passwordEncoder.encode(UUID.randomUUID().toString()), Role.PRESS);
        userRepository.save(user);

        LocalDateTime startTime = LocalDateTime.of(2022, Month.AUGUST, 16, 19, 30, 40);
        LocalDateTime endTime = LocalDateTime.of(2022, Month.AUGUST, 20, 19, 30, 40);

        LocationDto locationDto = new LocationDto("full address",10.0,10.0,"depth1","depth2");
        MissionRequestDto missionRequestDto = new MissionRequestDto("title","content",startTime, endTime, locationDto);

        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(missionRequestDto);

        //When
        this.mockMvc.perform(post("/breaking-mission")
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); //Then

        //Then
        Assertions.assertEquals(1, missionRepository.findAll().size());

    }

    @DisplayName("언론사가 아닌 개인유저가 브레이킹 미션을 게시할 경우, 예외가 발생한다.")
    @WithMockCustomUser
    @Transactional
    @Test
    void createMissionNotByPress() throws Exception {

        //Given
        User user = new User("12345g",passwordEncoder.encode(UUID.randomUUID().toString()), Role.USER);
        userRepository.save(user);

        LocalDateTime startTime = LocalDateTime.of(2022, Month.AUGUST, 16, 19, 30, 40);
        LocalDateTime endTime = LocalDateTime.of(2022, Month.AUGUST, 20, 19, 30, 40);

        LocationDto locationDto = new LocationDto("full address",10.0,10.0,"depth1","depth2");
        MissionRequestDto missionRequestDto = new MissionRequestDto("title","content",startTime, endTime, locationDto);

        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(missionRequestDto);

        //When
        this.mockMvc.perform(post("/breaking-mission")
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); //Then

    }

    @DisplayName("유효한 유저네임이 아닐 경우, 예외가 발생한다.")
    @WithMockCustomUser
    @Transactional
    @Test
    void createMissionWithInvalidUsername() throws Exception {

        //Given
        User user = new User("anotherUsername",passwordEncoder.encode(UUID.randomUUID().toString()), Role.PRESS);
        userRepository.save(user);

        LocalDateTime startTime = LocalDateTime.of(2022, Month.AUGUST, 16, 19, 30, 40);
        LocalDateTime endTime = LocalDateTime.of(2022, Month.AUGUST, 20, 19, 30, 40);

        LocationDto locationDto = new LocationDto("full address",10.0,10.0,"depth1","depth2");
        MissionRequestDto missionRequestDto = new MissionRequestDto("title","content",startTime, endTime, locationDto);

        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(missionRequestDto);

        //When
        this.mockMvc.perform(post("/breaking-mission")
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); //Then

    }

    @DisplayName("미션 게시 요청에 누락된 필드가 있을 경우, 예외가 발생한다.")
    @WithMockCustomUser
    @Transactional
    @Test
    void createMissionWithNullField() throws Exception {

        //Given
        User user = new User("12345g",passwordEncoder.encode(UUID.randomUUID().toString()), Role.PRESS);
        userRepository.save(user);

        LocalDateTime startTime = LocalDateTime.of(2022, Month.AUGUST, 16, 19, 30, 40);
        LocalDateTime endTime = LocalDateTime.of(2022, Month.AUGUST, 20, 19, 30, 40);

        LocationDto locationDto = new LocationDto("full address",10.0,10.0,"depth1",null);
        MissionRequestDto missionRequestDto = new MissionRequestDto("title","content",startTime, endTime, locationDto);

        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(missionRequestDto);

        //When
        this.mockMvc.perform(post("/breaking-mission")
                        .content(requestJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); //Then

    }

}