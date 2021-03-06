package com.dope.breaking.api;

import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostType;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.LocationDto;
import com.dope.breaking.dto.post.PostRequestDto;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.withMockCustomAuthorize.WithMockCustomUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;



@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@AutoConfigureMockMvc // @WebMvcTest??? ?????? ??? ???????????? ???????????? ?????? ????????? ????????? ????????? ?????? @Service??? @Repository??? ?????? ???????????? ?????? ???????????? ?????????. ???, ??????????????? ?????????????????? ???????????? ????????????.
class PostAPITest {

    @Autowired
    private MockMvc mockMvc; //???????????? ????????? ???????????? ????????? MockMvc??? ??????. ????????? ???????????? ??????????????? ??????????????? ????????? ????????? ????????? ????????????.

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PasswordEncoder passwordEncoder;


    @DisplayName("????????? ?????? ???????????? ????????? ???, ????????? ????????????.")
    @Test
    public void createPostWithoutLogin() throws Exception {
        MockMultipartFile multipartFile1 = new MockMultipartFile("file", "test1.png", "image/png", new FileInputStream(System.getProperty("user.dir") + "/src/test/java/com/dope/breaking/files/test1.png"));

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
                .andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();


        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);

    }


    @DisplayName("????????? ??? ???????????? ????????? ??? ??????????????? ????????????.")
    @WithMockCustomUser
    @Test
    public void createPostWithLogin() throws Exception {

        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // ?????? ????????? USER ??? ??????
                .build();

        userRepository.save(user);

        MockMultipartFile multipartFile1 = new MockMultipartFile("file", "test1.png", "image/png", new FileInputStream(System.getProperty("user.dir") + "/src/test/java/com/dope/breaking/files/test1.png"));

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
        System.out.println(content);

    }



    @DisplayName("???????????? ?????? ???????????? ??????????????? ??? ???, ????????? ????????????.")
    @WithMockCustomUser
    @Test
    public void modifyNoSuchPost () throws Exception {
        LocationDto location = LocationDto.builder()
                .longitude(1.2)
                .region("andong")
                .latitude(1.3).build();
        List<String> hashTags = new LinkedList<>();
        hashTags.add("tag2");


        PostRequestDto postRequestDto = PostRequestDto.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType("free")
                .eventTime(LocalDateTime.parse("2016-10-31 23:59:59",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .locationDto(location)
                .hashtagList(hashTags).build();

        System.out.println(postRequestDto.toString());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(postRequestDto);

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart("/post/" + -1);
        builder.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PUT");
                return request;
            }
        });


        MvcResult resultActions = this.mockMvc.perform(builder
                        .content(content)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String contentBody = response.getContentAsString();
        System.out.println(contentBody);
    }





    @DisplayName("????????? ???????????? ???????????? ??? ???, ????????? ????????????.")
    @WithMockCustomUser
    @Test
    public void modifyNoPermission() throws Exception {
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // ?????? ????????? USER ??? ??????
                .build();

        userRepository.save(user);



        Post post = new Post();
        long postId = postRepository.save(post).getId();
        User user2 = User.builder()
                .username("123")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.USER).build();
        userRepository.save(user2);
        post.setUser(user2);

        LocationDto location = LocationDto.builder()
                .longitude(1.2)
                .region("andong")
                .latitude(1.3).build();
        List<String> hashTags = new LinkedList<>();
        hashTags.add("tag2");


        PostRequestDto postRequestDto = PostRequestDto.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType("free")
                .eventTime(LocalDateTime.parse("2016-10-31 23:59:59",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .locationDto(location)
                .hashtagList(hashTags).build();

        System.out.println(postRequestDto.toString());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(postRequestDto);

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
                        .content(content)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotAcceptable()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String contentBody = response.getContentAsString();
        System.out.println(contentBody);

    }

    @DisplayName("???????????? ???????????? ???????????? ??? ???, ??????????????? ????????????.")
    @WithMockCustomUser
    @Test
    public void modifyPost() throws Exception {
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // ?????? ????????? USER ??? ??????
                .build();

        userRepository.save(user);




        Post post = new Post();
        long postId = postRepository.save(post).getId();
        post.setUser(user);
        LocationDto location = LocationDto.builder()
                .longitude(1.2)
                .region("andong")
                .latitude(1.3).build();
        List<String> hashTags = new LinkedList<>();
        hashTags.add("tag2");


        PostRequestDto postRequestDto = PostRequestDto.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType("free")
                .eventTime(LocalDateTime.parse("2016-10-31 23:59:59",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .locationDto(location)
                .hashtagList(hashTags).build();

        System.out.println(postRequestDto.toString());
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(postRequestDto);

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
                        .content(content)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String contentBody = response.getContentAsString();
        System.out.println(contentBody);

    }


    @DisplayName("???????????? ?????? ???????????? ???????????? ??? ???, ????????? ????????????.")
    @Test
    public void readNoSuchPost() throws Exception {

        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/post/" + -1))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);
    }






    @DisplayName("????????? ???????????? ???????????? ????????????.")
    @Test
    public void readPostWithAnonymous() throws Exception {

        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // ?????? ????????? USER ??? ??????
                .build();

        userRepository.save(user);
        Location location = Location.builder()
                .longitude(1.2)
                .region("andong")
                .latitude(1.3).build();


        Post post= Post.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType(PostType.FREE)
                .eventTime(LocalDateTime.parse("2016-10-31 23:59:59",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .location(location)
                .build();


        long postId = postRepository.save(post).getId();
        post.setUser(user);


        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/post/" + postId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isLiked").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.isBookmarked").value(false))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);
    }


    @DisplayName("???????????? ???????????? ???????????? ????????????.")
    @WithMockCustomUser
    @Test
    public void readPostWithAuthentication() throws Exception {
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // ?????? ????????? USER ??? ??????
                .build();

        userRepository.save(user);

        Location location = Location.builder()
                .longitude(1.2)
                .region("andong")
                .latitude(1.3).build();


        Post post= Post.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType(PostType.FREE)
                .eventTime(LocalDateTime.parse("2016-10-31 23:59:59",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .location(location)
                .build();

        long postId = postRepository.save(post).getId();
        post.setUser(user);

        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/post/" + postId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isLiked").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.isBookmarked").value(false))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);
    }






    @DisplayName("???????????? ?????? ???????????? ????????? ???, ????????? ????????????.")
    @WithMockCustomUser
    @Test
    public void deleteNoSuchPost() throws Exception {


        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.delete("/post/" + -1))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);
    }

    @DisplayName("????????? ???????????? ????????? ???, ????????? ????????????.")
    @WithMockCustomUser
    @Test
    public void deleteNoPermission() throws Exception {
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // ?????? ????????? USER ??? ??????
                .build();

        userRepository.save(user);



        Post post = new Post();
        long postId = postRepository.save(post).getId();
        User user1 = User.builder()
                .username("123")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.USER).build();
        userRepository.save(user1);
        post.setUser(user1);

        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.delete("/post/" + postId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotAcceptable()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);
    }


    @DisplayName("???????????? ???????????? ????????? ???, ????????????.")
    @WithMockCustomUser
    @Test
    public void deletePost() throws Exception {
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // ?????? ????????? USER ??? ??????
                .build();

        userRepository.save(user);



        Post post = new Post();
        long postId = postRepository.save(post).getId();
        post.setUser(user);
        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.delete("/post/" + postId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);
    }



}