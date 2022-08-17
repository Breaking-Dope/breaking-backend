package com.dope.breaking.api;

import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.domain.post.PostType;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.mission.MissionRequestDto;
import com.dope.breaking.dto.post.LocationDto;
import com.dope.breaking.repository.MissionRepository;
import com.dope.breaking.repository.PostRepository;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
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
    private PostRepository postRepository;

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

    @DisplayName("missionId가 유효하지 않은 경우, 예외가 발생한다.")
    @WithMockCustomUser
    @Transactional
    @Test
    void submitPostForMission() throws Exception {

        //Given
        User user = new User("12345g",passwordEncoder.encode(UUID.randomUUID().toString()), Role.USER);
        userRepository.save(user);

        User press = new User();
        userRepository.save(press);

        Mission mission = new Mission(user,"title","content",null,null,null);
        missionRepository.save(mission);

        MockMultipartFile multipartFile1 = new MockMultipartFile("file", "test1.png", "image/png", new FileInputStream(System.getProperty("user.dir") + "/src/test/java/com/dope/breaking/files/test1.png"));

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 123," +
                "\"isAnonymous\" : \"false\"," +
                "\"postType\" : \"mission\"," +
                "\"eventDate\" : \"2020-01-01 14:01:01\"," +
                "\"location\" : {" +
                " \"address\" : \"address\"," +
                "\"longitude\" : 12.1234," +
                "\"latitude\" : 12.12345," +
                "\"region_1depth_name\" : \"region_1depth_name\"," +
                "\"region_2depth_name\" : \"region_2depth_name\" " +
                "}," +
                "\"hashtagList\" : [" +
                "\"hello\", \"hello2\"]," +
                "\"thumbnailIndex\" : 0" +
                "}";

        //When
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/breaking-mission/{missionId}", mission.getId())
                        .file(multipartFile1)
                        .part(new MockPart("data", json.getBytes(StandardCharsets.UTF_8)))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .characterEncoding("UTF-8"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());  //Then

        //Then
        Assertions.assertEquals(PostType.MISSION,postRepository.findAll().get(0).getPostType());
        Assertions.assertEquals(mission,postRepository.findAll().get(0).getMission());

    }

    @DisplayName("유저네임이 일치할 때 미션을 수행할 경우, 제보가 문제 없이 생성된다.")
    @WithMockCustomUser
    @Transactional
    @Test
    void submitPostForMissionWithInvalidMissionId() throws Exception {

        //Given
        User user = new User("12345g",passwordEncoder.encode(UUID.randomUUID().toString()), Role.USER);
        userRepository.save(user);

        User press = new User();
        userRepository.save(press);

        Mission mission = new Mission(user,"title","content",null,null,null);
        missionRepository.save(mission);

        MockMultipartFile multipartFile1 = new MockMultipartFile("file", "test1.png", "image/png", new FileInputStream(System.getProperty("user.dir") + "/src/test/java/com/dope/breaking/files/test1.png"));

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 123," +
                "\"isAnonymous\" : \"false\"," +
                "\"postType\" : \"mission\"," +
                "\"eventDate\" : \"2020-01-01 14:01:01\"," +
                "\"location\" : {" +
                " \"address\" : \"address\"," +
                "\"longitude\" : 12.1234," +
                "\"latitude\" : 12.12345," +
                "\"region_1depth_name\" : \"region_1depth_name\"," +
                "\"region_2depth_name\" : \"region_2depth_name\" " +
                "}," +
                "\"hashtagList\" : [" +
                "\"hello\", \"hello2\"]," +
                "\"thumbnailIndex\" : 0" +
                "}";

        //When
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/breaking-mission/{missionId}", 100L)
                        .file(multipartFile1)
                        .part(new MockPart("data", json.getBytes(StandardCharsets.UTF_8)))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .characterEncoding("UTF-8"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest()); //Then

    }

}