package com.dope.breaking.api;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ProfileInformationResponseDto;
import com.dope.breaking.dto.user.SignUpRequestDto;
import com.dope.breaking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserAPITest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

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

}