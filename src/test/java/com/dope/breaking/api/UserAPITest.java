package com.dope.breaking.api;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ProfileInformationResponseDto;
import com.dope.breaking.dto.user.SignUpRequestDto;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.withMockCustomAuthorize.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserAPITest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void getProfileInformationSuccess() throws Exception {
        User user = new User();
        SignUpRequestDto signUpRequest =  new SignUpRequestDto
                ("statusMsg","nickname","phoneNumber","test@email.com","realname","testUsername", "press");
        user.setRequestFields(
                "anyURL",
                "anyURL",
                signUpRequest.getNickname(),
                signUpRequest.getPhoneNumber(),
                signUpRequest.getEmail(),
                signUpRequest.getRealName(),
                signUpRequest.getStatusMsg(),
                signUpRequest.getUsername(),
                Role.valueOf(signUpRequest.getRole().toUpperCase(Locale.ROOT))
        );
        userRepository.save(user);

        mockMvc.perform(get("/profile/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()));

    }


    @Test
    void getProfileInformationFailure() throws Exception {
        User user = new User();
        SignUpRequestDto signUpRequest =  new SignUpRequestDto
                ("statusMsg","nickname","phoneNumber","test@email.com","realname","testUsername", "press");
        user.setRequestFields(
                "anyURL",
                "anyURL",
                signUpRequest.getNickname(),
                signUpRequest.getPhoneNumber(),
                signUpRequest.getEmail(),
                signUpRequest.getRealName(),
                signUpRequest.getStatusMsg(),
                signUpRequest.getUsername(),
                Role.valueOf(signUpRequest.getRole().toUpperCase(Locale.ROOT))
        );
        userRepository.save(user);

        mockMvc.perform(get("/profile/" + 1000000000))
                .andExpect(status().isNotFound());

    }

    @DisplayName("유저를 회원탈퇴 시킨다.")
    @Test
    @WithMockCustomUser
    void signOutSuccess() throws Exception {
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.USER) // 최초 가입시 USER 로 설정
                .build();

        userRepository.save(user);

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/oauth2/withdraw"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @DisplayName("회원 탈퇴 시, 유저가 존재하지 않으면 예외가 발생한다.")
    @Test
    @WithMockCustomUser
    void signOutFailure() throws Exception {

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/oauth2/sign-out"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is4xxClientError());
    }

}