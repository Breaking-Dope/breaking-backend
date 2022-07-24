package com.dope.breaking.api;

import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostLike;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.PostRequestDto;
import com.dope.breaking.repository.PostLikeRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.security.jwt.JwtTokenProvider;
import com.dope.breaking.service.MediaService;
import com.dope.breaking.service.PostService;
import com.dope.breaking.service.UserService;
import com.dope.breaking.withMockCustomAuthorize.WithMockCustomUser;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc // @WebMvcTest와 가장 큰 차이점은 컨트롤러 뿐만 아니라 테스트 대상이 아닌 @Service나 @Repository가 붙은 객체들도 모두 메모리에 올린다. 즉, 서비스단과 컨트롤러단의 테스트가 가능해짐.
class PostAPITest {

    @Autowired
    private MockMvc mockMvc; //컨트롤러 테스트 하기위한 도구인 MockMvc를 사용. 위에서 자동으로 설정해주는 어노테이션 덕분에 의존성 주입이 필요없다.

    @Autowired
    PostAPI postAPI;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostService postService;

    @Autowired
    MediaService mediaService;

    @Autowired
    PostRepository postRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    PostLikeRepository postLikeRepository;

    @Autowired
    UserService userService;

    static Long postId;

    @Order(1)
    @Test
    public void createUserInfo() {
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);
    }

    @DisplayName("POST 등록 테스트")
    @Order(2)
    @WithMockCustomUser
    @Test
    public void testpost() throws Exception {
        MockMultipartFile multipartFile1 = new MockMultipartFile("file", "test1.png", "image/png", new FileInputStream(System.getProperty("user.dir") + "/src/test/java/com/dope/breaking/files/test1.png"));
        Location location = Location.builder()
                .longitude(1.2)
                .region("andong")
                .latitude(1.3).build();
        List<String> hashTags = new LinkedList<>();
        hashTags.add("tag1");

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 123," +
                "\"isAnonymous\" : \"false\"," +
                "\"postType\" : \"free\"," +
                "\"eventTime\" : \"2020-01-01 14:01:01\"," +
                "\"location\" : {" +
                " \"region\" : \"abgujung\"," +
                "\"longitude\" : 12.1234," +
                "\"latitude\" : 12.12345" +
                "}," +
                "\"hashtagList\" : [" +
                "\"hello\", \"hello2\"]," +
                "\"thumbnailIndex\" : 0" +
                "}";

        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.multipart("/post")
                        .file(multipartFile1)
                        .part(new MockPart("data", json.getBytes(StandardCharsets.UTF_8)))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .characterEncoding("UTF-8"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();


        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(content);
        System.out.println(jsonObject.toJSONString());
        postId = Long.parseLong(jsonObject.get("postId").toString());
        System.out.println(postId);

    }


    @DisplayName("POST 수정 테스트")
    @Order(3)
    @WithMockCustomUser
    @Test
    public void testmodify() throws Exception {
        MockMultipartFile multipartFile1 = new MockMultipartFile("file", "test2.png", "image/png", new FileInputStream(System.getProperty("user.dir") + "/src/test/java/com/dope/breaking/files/test2.png"));
        Location location = Location.builder()
                .longitude(1.2)
                .region("andong")
                .latitude(1.3).build();
        List<String> hashTags = new LinkedList<>();
        hashTags.add("tag2");

        String json = "{" +
                "\"title\" : \"수정\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 123," +
                "\"isAnonymous\" : \"false\"," +
                "\"postType\" : \"free\"," +
                "\"eventTime\" : \"2020-01-01 14:01:01\"," +
                "\"location\" : {" +
                " \"region\" : \"abgujung\"," +
                "\"longitude\" : 12.1234," +
                "\"latitude\" : 12.12345" +
                "}," +
                "\"hashtagList\" : [" +
                "\"hello\", \"hello2\"]," +
                "\"thumbnailIndex\" : 0" +
                "}";
        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart("/post/" + postId);
        builder.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PUT");
                return request;
            }
        });


        MvcResult resultActions = this.mockMvc.perform(builder
                        .file(multipartFile1)
                        .part(new MockPart("data", json.getBytes(StandardCharsets.UTF_8)))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .characterEncoding("UTF-8"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
    }


    @DisplayName("POST 조회 기능")
    @WithMockCustomUser
    @Order(4)
    @Test
    public void readPostWithAnonymous() throws Exception {
        //When

        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/post/" + postId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isLiked").value(false))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);
    }



}