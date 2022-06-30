package com.dope.breaking.api;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.SignUpRequestDto;
import com.dope.breaking.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserAPITest {

    @Autowired
    UserService userService;

    @Test
    void signUpConfirm() {

        // Given
        SignUpRequestDto signUpRequest =  new SignUpRequestDto
                ("URL","nickname","phoneNumber", "mwk300@nyu.edu","Minwu","Kim","msg","username");

        // When
        User user = new User();

        user.signUp(
                signUpRequest.getProfileImgURL(),
                signUpRequest.getNickname(),
                signUpRequest.getPhoneNumber(),
                signUpRequest.getEmail(),
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getStatusMsg(),
                signUpRequest.getUsername()
        );

        User savedUser = userService.save(user);

        // Then
        User foundUser = userService.findById(savedUser.getId()).get();
        Assertions.assertThat(foundUser).isEqualTo(user);

    }

}