package com.dope.breaking.api;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.security.jwt.JwtTokenProvider;
import com.dope.breaking.service.UserService;
import com.dope.breaking.withMockCustomAuthorize.WithMockCustomUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.FileInputStream;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc // @WebMvcTest와 가장 큰 차이점은 컨트롤러 뿐만 아니라 테스트 대상이 아닌 @Service나 @Repository가 붙은 객체들도 모두 메모리에 올린다. 즉, 서비스단과 컨트롤러단의 테스트가 가능해짐.
class PostAPITest {

    @Autowired
    private MockMvc mockMvc; //컨트롤러 테스트 하기위한 도구인 MockMvc를 사용. 위에서 자동으로 설정해주는 어노테이션 덕분에 의존성 주입이 필요없다.

    @Autowired
    PostAPI postAPI;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;


    @Autowired
    PasswordEncoder passwordEncoder;


    private String jwt;

    @BeforeEach
    public void createToken() {
      User  user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
                .build();

        userService.save(user);
    }


    @AfterEach
    public void aftercleanup() {
        userRepository.deleteAll();
    }

    @DisplayName("POST 등록 테스트")
    @WithMockCustomUser
    @Test
    public void testpost() throws Exception{
        MockMultipartFile multipartFile1 = new MockMultipartFile("file", "test1.jpg", "image/png", new FileInputStream(System.getProperty("user.dir") + "/files/test1.png"));

        MvcResult resultActions = this.mockMvc.perform(MockMvcRequestBuilders.multipart("/posttest")
                        .file(multipartFile1)
                        .content("hello")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .characterEncoding("UTF-8"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();


        MockHttpServletResponse response = resultActions.getResponse();
        String content = response.getContentAsString();

        Assertions.assertThat(content.contains("hello")).isTrue();

    }
}