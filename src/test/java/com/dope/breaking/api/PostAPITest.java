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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@AutoConfigureMockMvc // @WebMvcTest와 가장 큰 차이점은 컨트롤러 뿐만 아니라 테스트 대상이 아닌 @Service나 @Repository가 붙은 객체들도 모두 메모리에 올린다. 즉, 서비스단과 컨트롤러단의 테스트가 가능해짐.
class PostAPITest {

    @Autowired
    private MockMvc mockMvc; //컨트롤러 테스트 하기위한 도구인 MockMvc를 사용. 위에서 자동으로 설정해주는 어노테이션 덕분에 의존성 주입이 필요없다.

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PasswordEncoder passwordEncoder;


    @DisplayName("로그인 없이 게시글을 생성할 시, 예외가 반환된다.")
    @Test
    public void createPostWithoutLogin() throws Exception {
        MockMultipartFile multipartFile1 = new MockMultipartFile("file", "test1.png", "image/png", new FileInputStream(System.getProperty("user.dir") + "/src/test/java/com/dope/breaking/files/test1.png"));

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 123," +
                "\"isAnonymous\" : \"false\"," +
                "\"postType\" : \"free\"," +
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

        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.multipart("/post")
                        .file(multipartFile1)
                        .part(new MockPart("data", json.getBytes(StandardCharsets.UTF_8)))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .characterEncoding("UTF-8"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is4xxClientError()).andReturn();


        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);

    }


    @DisplayName("로그인 후 게시글을 생성할 시 정상적으로 등록된다.")
    @WithMockCustomUser
    @Test
    public void createPostWithLogin() throws Exception {

        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);

        MockMultipartFile multipartFile1 = new MockMultipartFile("file", "test1.png", "image/png", new FileInputStream(System.getProperty("user.dir") + "/src/test/java/com/dope/breaking/files/test1.png"));

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 123," +
                "\"isAnonymous\" : \"false\"," +
                "\"postType\" : \"free\"," +
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

        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.multipart("/post")
                        .file(multipartFile1)
                        .part(new MockPart("data", json.getBytes(StandardCharsets.UTF_8)))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .characterEncoding("UTF-8"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated()).andReturn();


        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);

    }



    @DisplayName("존재하지 않는 게시글을 수정하려고 할 시, 예외가 반환된다.")
    @WithMockCustomUser
    @Test
    public void modifyNoSuchPost () throws Exception {
        LocationDto location = LocationDto.builder()
                .longitude(1.2)
                .address("andong")
                .latitude(1.3)
                .region_1depth_name("region1")
                .region_2depth_name("region2")
                .build();

        List<String> hashTags = new LinkedList<>();
        hashTags.add("tag2");


        PostRequestDto postRequestDto = PostRequestDto.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType("free")
                .eventDate(LocalDateTime.parse("2016-10-31 23:59:59",
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
                .andExpect(status().isNotFound()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String contentBody = response.getContentAsString();
        System.out.println(contentBody);
    }





    @DisplayName("타인의 게시글을 수정하려 할 시, 예외가 반환된다.")
    @WithMockCustomUser
    @Test
    public void modifyNoPermission() throws Exception {
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
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
                .address("andong")
                .latitude(1.3)
                .region_1depth_name("region1")
                .region_2depth_name("region2")
                .build();
        List<String> hashTags = new LinkedList<>();
        hashTags.add("tag2");


        PostRequestDto postRequestDto = PostRequestDto.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType("free")
                .eventDate(LocalDateTime.parse("2016-10-31 23:59:59",
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
                .andExpect(status().isNotAcceptable()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String contentBody = response.getContentAsString();
        System.out.println(contentBody);

    }

    @DisplayName("작성자가 게시글을 수정하려 할 시, 정상적으로 수정된다.")
    @WithMockCustomUser
    @Test
    public void modifyPost() throws Exception {
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);




        Post post = new Post();
        long postId = postRepository.save(post).getId();
        post.setUser(user);
        LocationDto location = LocationDto.builder()
                .longitude(1.2)
                .address("andong")
                .latitude(1.3)
                .region_1depth_name("region1")
                .region_2depth_name("region2")
                .build();
        List<String> hashTags = new LinkedList<>();
        hashTags.add("tag2");


        PostRequestDto postRequestDto = PostRequestDto.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType("free")
                .eventDate(LocalDateTime.parse("2016-10-31 23:59:59",
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
                .andExpect(status().isOk()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String contentBody = response.getContentAsString();
        System.out.println(contentBody);

    }


    @DisplayName("존재하지 않는 게시글을 접근하려 할 시, 예외가 반환된다.")
    @Test
    public void readNoSuchPost() throws Exception {

        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/post/" + -1))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);
    }






    @DisplayName("익명의 사용자가 게시글을 조회힌다.")
    @Test
    public void readPostWithAnonymous() throws Exception {

        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);
        Location location = Location.builder()
                .longitude(1.2)
                .address("andong")
                .latitude(1.3)
                .region_1depth_name("region1")
                .region_2depth_name("region2")
                .build();


        Post post= Post.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType(PostType.FREE)
                .eventDate(LocalDateTime.parse("2016-10-31 23:59:59",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .location(location)
                .build();


        long postId = postRepository.save(post).getId();
        post.setUser(user);


        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/post/" + postId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isLiked").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.isBookmarked").value(false))
                .andExpect(status().isOk()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);
    }


    @DisplayName("로그인된 사용자가 게시글을 조회힌다.")
    @WithMockCustomUser
    @Test
    public void readPostWithAuthentication() throws Exception {
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);

        Location location = Location.builder()
                .longitude(1.2)
                .address("andong")
                .latitude(1.3)
                .region_1depth_name("region1")
                .region_2depth_name("region2")
                .build();


        Post post= Post.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType(PostType.FREE)
                .eventDate(LocalDateTime.parse("2016-10-31 23:59:59",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .location(location)
                .build();

        long postId = postRepository.save(post).getId();
        post.setUser(user);

        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.get("/post/" + postId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isLiked").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.isBookmarked").value(false))
                .andExpect(status().isOk()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);
    }






    @DisplayName("존재하지 않는 게시글을 삭제할 시, 예외가 반환된다.")
    @WithMockCustomUser
    @Test
    public void deleteNoSuchPost() throws Exception {


        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.delete("/post/" + -1))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);
    }

    @DisplayName("타인의 게시글을 삭제할 시, 예외가 반환된다.")
    @WithMockCustomUser
    @Test
    public void deleteNoPermission() throws Exception {
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
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
                .andExpect(status().isNotAcceptable()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);
    }


    @DisplayName("작성자가 게시글을 삭제할 시, 삭제된다.")
    @WithMockCustomUser
    @Test
    public void deletePost() throws Exception {
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);

        Post post = new Post();
        long postId = postRepository.save(post).getId();
        post.setUser(user);
        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.delete("/post/" + postId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()).andReturn();

        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();
        System.out.println(content);

    }

    @DisplayName("구매 비활성화 된 제보를 활성화 할 경우, 제보가 활성화 된다.")
    @WithMockCustomUser
    @Test
    void activatePurchasedDeactivatedPost() throws Exception {

        //Given
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);

        Post post = new Post();
        post.setUser(user);
        post.updateIsPurchasable(false);
        Long postId = postRepository.save(post).getId();


        //When
        this.mockMvc.perform(MockMvcRequestBuilders.post("/post/{postId}/activate-purchase",postId))
                .andExpect(status().isOk()); //Then

        //Then
        Assertions.assertTrue(post.getIsPurchasable());

    }

    @DisplayName("구매 활성화 된 제보를 활성화 할 경우, 예외가 발생한다.")
    @WithMockCustomUser
    @Test
    void activatePurchaseActivatedPost() throws Exception {

        //Given
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);

        Post post = new Post();
        post.setUser(user);
        Long postId = postRepository.save(post).getId();


        //When
        this.mockMvc.perform(MockMvcRequestBuilders.post("/post/{postId}/activate-purchase",postId))
                .andExpect(status().isBadRequest()); //Then


    }

    @DisplayName("다른 유저의 제보를 구매 활성화 할 경우, 예외가 발생한다.")
    @WithMockCustomUser
    @Test
    void activateOthersPost() throws Exception {

        //Given
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
                .build();

        User user2 = new User();

        userRepository.save(user);
        userRepository.save(user2);

        Post post = new Post();
        post.setUser(user2);
        post.updateIsPurchasable(false);
        Long postId = postRepository.save(post).getId();

        //When
        this.mockMvc.perform(MockMvcRequestBuilders.post("/post/{postId}/activate-purchase",postId))
                .andExpect(status().isNotAcceptable()); //Then

    }

    @DisplayName("구매 활성화가 된 제보를 구매 비활성화 할 경우, 제보 구매가 비활성화 된다.")
    @WithMockCustomUser
    @Test
    void deactivatePurchaseActivatedPost() throws Exception {

        //Given
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);

        Post post = new Post();
        post.setUser(user);
        Long postId = postRepository.save(post).getId();

        //When
        this.mockMvc.perform(MockMvcRequestBuilders.post("/post/{postId}/deactivate-purchase",postId))
                .andExpect(status().isOk()); //Then

        //Then
        Assertions.assertFalse(post.getIsPurchasable());

    }

    @DisplayName("이미 구매 비활성화가 된 제보를 구매 비활성화 할 경우, 예외가 발생한다.")
    @WithMockCustomUser
    @Test
    void deactivatePurchaseDeactivatedPost() throws Exception {

        //Given
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);

        Post post = new Post();
        post.setUser(user);
        post.updateIsPurchasable(false);
        Long postId = postRepository.save(post).getId();

        //When
        this.mockMvc.perform(MockMvcRequestBuilders.post("/post/{postId}/deactivate-purchase",postId))
                .andExpect(status().isBadRequest()); //Then

    }

    @DisplayName("공개 된 제보를 숨길 경우, 정상적으로 숨김처리 된다.")
    @WithMockCustomUser
    @Test
    void hideNotHiddenPost() throws Exception {

        //Given
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);

        Post post = new Post();
        post.setUser(user);
        post.updateIsHidden(false);
        Long postId = postRepository.save(post).getId();

        //When
        this.mockMvc.perform(MockMvcRequestBuilders.post("/post/{postId}/hide",postId))
                .andExpect(status().isOk()); //Then

        //Then
        Assertions.assertTrue(postRepository.getById(postId).isHidden());

    }

    @DisplayName("숨김 처리 된 제보를 공개할 경우, 정상적으로 공개처리 된다.")
    @WithMockCustomUser
    @Test
    void cancelHideHiddenPost() throws Exception {

        //Given
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);

        Post post = new Post();
        post.setUser(user);
        post.updateIsHidden(true);
        Long postId = postRepository.save(post).getId();

        //When
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/post/{postId}/hide",postId))
                .andExpect(status().isOk()); //Then

        //Then
        Assertions.assertFalse(postRepository.getById(postId).isHidden());

    }


}