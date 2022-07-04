package com.dope.breaking.service;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.SignUpRequestDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired UserService userService;

    @Test
    void isValidEmail() {

        String email1 = "hello@naver.com";
        String email2 = "hello@naver";
        String email3 = "hello";

        assertTrue(userService.isValidEmailFormat(email1));
        assertFalse(userService.isValidEmailFormat(email2));
        assertFalse(userService.isValidEmailFormat(email3));

    }

    @Test
    void isValidRole() {

        String role1 = "PRESS";
        String role2 = "PreSs";
        String role3 = "Pre";

        assertTrue(userService.isValidRole(role1));
        assertTrue(userService.isValidRole(role2));
        assertFalse(userService.isValidRole(role3));

    }

    @Test
    void signUpConfirm() {

        // Given
        SignUpRequestDto signUpRequest =  new SignUpRequestDto
                ("nickname","phoneNumber", "mwk300@nyu.edu","Minwu","Kim","msg","username", "PRess");

        // When
        User user = new User();

        user.signUp(
                "anyURL",
                signUpRequest.getNickname(),
                signUpRequest.getPhoneNumber(),
                signUpRequest.getEmail(),
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getStatusMsg(),
                signUpRequest.getUsername(),
                Role.valueOf(signUpRequest.getRole().toUpperCase(Locale.ROOT))

        );

        User savedUser = userService.save(user);

        // Then
        User foundUser = userService.findById(savedUser.getId()).get();
        Assertions.assertThat(foundUser).isEqualTo(user);

    }

}