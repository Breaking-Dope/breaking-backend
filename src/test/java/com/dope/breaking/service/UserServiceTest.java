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
    void isValidEmailFormat() {

        String email1 = "hello@naver.com";
        String email2 = "hello@naver";
        String email3 = "hello";

        assertTrue(userService.isValidEmailFormat(email1));
        assertFalse(userService.isValidEmailFormat(email2));
        assertFalse(userService.isValidEmailFormat(email3));

    }

    @Test
    void isValidPhoneNumberFormat() {

        String number1 = "01012345678";
        String number2 = "0212345678";
        String number3 = "010102312";

        assertTrue(userService.isValidPhoneNumberFormat(number1));
        assertTrue(userService.isValidPhoneNumberFormat(number2));
        assertFalse(userService.isValidPhoneNumberFormat(number3));

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
                ("statusMsg","nickname","phoneNumber","mwk300@nyu.edu","realname","username", "PRess");

        // When
        User user = new User();
        user.setRequestFields(
                "anyURL",
                signUpRequest.getNickname(),
                signUpRequest.getPhoneNumber(),
                signUpRequest.getEmail(),
                signUpRequest.getRealName(),
                signUpRequest.getStatusMsg(),
                signUpRequest.getUsername(),
                Role.valueOf(signUpRequest.getRole().toUpperCase(Locale.ROOT))

        );

        User savedUser = userService.save(user);

        // Then
        User foundUser = userService.findById(savedUser.getId()).get();
        Assertions.assertThat(foundUser).isEqualTo(user);

    }

    @Test
    void updateUser(){
        // Given
        SignUpRequestDto signUpRequest =  new SignUpRequestDto
                ("statusMsg","nickname","phoneNumber","mwk300@nyu.edu","realname","username", "PRess");

        User user = new User();
        user.setRequestFields(
                "anyURL",
                signUpRequest.getNickname(),
                signUpRequest.getPhoneNumber(),
                signUpRequest.getEmail(),
                signUpRequest.getRealName(),
                signUpRequest.getStatusMsg(),
                signUpRequest.getUsername(),
                Role.valueOf(signUpRequest.getRole().toUpperCase(Locale.ROOT))
        );

        userService.save(user);

        //When
        User updatedUser = userService.findById(1L).get();
        updatedUser.setRequestFields(
                "anyURL",
                "newNickname",
                signUpRequest.getPhoneNumber(),
                signUpRequest.getEmail(),
                signUpRequest.getRealName(),
                signUpRequest.getStatusMsg(),
                signUpRequest.getUsername(),
                Role.valueOf(signUpRequest.getRole().toUpperCase(Locale.ROOT))
        );

        userService.save(updatedUser);

        //Then
        User foundUser = userService.findById(1L).get();

        Assertions.assertThat(foundUser.getId()).isEqualTo(1L);
        Assertions.assertThat(foundUser.getNickname()).isEqualTo("newNickname");

    }

}