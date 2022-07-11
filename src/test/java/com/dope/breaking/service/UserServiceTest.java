package com.dope.breaking.service;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.SignUpRequestDto;
import com.dope.breaking.dto.user.UserBriefInformationResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired UserService userService;
    @Autowired EntityManager em;

//    @Test
//    void isValidEmailFormat() {
//
//        String email1 = "hello@naver.com";
//        String email2 = "hello@naver";
//        String email3 = "hello";
//
//        assertTrue(userService.isValidEmailFormat(email1));
//        assertFalse(userService.isValidEmailFormat(email2));
//        assertFalse(userService.isValidEmailFormat(email3));
//
//    }
//
//    @Test
//    void isValidPhoneNumberFormat() {
//
//        String number1 = "01012345678";
//        String number2 = "0212345678";
//        String number3 = "010102312";
//
//        assertTrue(userService.isValidPhoneNumberFormat(number1));
//        assertTrue(userService.isValidPhoneNumberFormat(number2));
//        assertFalse(userService.isValidPhoneNumberFormat(number3));
//
//    }
//
//    @Test
//    void isValidRole() {
//
//        String role1 = "PRESS";
//        String role2 = "PreSs";
//        String role3 = "Pre";
//
//        assertTrue(userService.isValidRole(role1));
//        assertTrue(userService.isValidRole(role2));
//        assertFalse(userService.isValidRole(role3));
//
//    }

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
        assertThat(foundUser).isEqualTo(user);

    }

//    @Test
//    void updateUser(){
//        // Given
//        SignUpRequestDto signUpRequest =  new SignUpRequestDto
//                ("statusMsg","nickname","phoneNumber","mwk300@nyu.edu","realname","username", "PRess");
//
//        User user = new User();
//        user.setRequestFields(
//                "anyURL",
//                signUpRequest.getNickname(),
//                signUpRequest.getPhoneNumber(),
//                signUpRequest.getEmail(),
//                signUpRequest.getRealName(),
//                signUpRequest.getStatusMsg(),
//                signUpRequest.getUsername(),
//                Role.valueOf(signUpRequest.getRole().toUpperCase(Locale.ROOT))
//        );
//
//        userService.save(user);
//
//        //When
//        User updatedUser = userService.findById(1L).get();
//        updatedUser.setRequestFields(
//                "anyURL",
//                "newNickname",
//                signUpRequest.getPhoneNumber(),
//                signUpRequest.getEmail(),
//                signUpRequest.getRealName(),
//                signUpRequest.getStatusMsg(),
//                signUpRequest.getUsername(),
//                Role.valueOf(signUpRequest.getRole().toUpperCase(Locale.ROOT))
//        );
//
//        userService.save(updatedUser);
//
//        //Then
//        User foundUser = userService.findById(1L).get();
//
//        assertThat(foundUser.getId()).isEqualTo(1L);
//        assertThat(foundUser.getNickname()).isEqualTo("newNickname");
//
//    }

    @Test
    void validateJwtTokenSuccess() {

        //Given
        User user = new User();
        SignUpRequestDto signUpRequest =  new SignUpRequestDto
                ("statusMsg","nickname","phoneNumber","mwk300@nyu.edu","realname","testUsername", "press");
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
        UserBriefInformationResponseDto foundUserInfo = userService.userBriefInformation("testUsername");

        //Then
        assertEquals(foundUserInfo.getUserId(), user.getId());
        assertEquals(foundUserInfo.getNickname(), user.getNickname());
        assertEquals(foundUserInfo.getProfileImgURL(), user.getProfileImgURL());
    }

    @Test
    void validateJwtTokenFailureNotFoundUserName() {

        //Given
        User user = new User();
        SignUpRequestDto signUpRequest =  new SignUpRequestDto
                ("statusMsg","nickname","phoneNumber","mwk300@nyu.edu","realname","testUsername", "press");
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

        //Then
        org.junit.jupiter.api.Assertions.assertThrows(InvalidAccessTokenException.class, () -> {
            //When
            userService.userBriefInformation("notFoundTestUsername");
        });
    }

    @Test
    void validateJwtTokenFailureNotMatchedUserName() {

        //Given
        User user = new User();
        SignUpRequestDto signUpRequest =  new SignUpRequestDto
                ("statusMsg","nickname","phoneNumber","mwk300@nyu.edu","realname","testUsername", "press");
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

        User anotherUser = new User();
        SignUpRequestDto anotherSignUpRequest =  new SignUpRequestDto
                ("statusMsg","nickname","phoneNumber","mwk300@nyu.edu","realname","anotherTestUsername", "press");
        anotherUser.setRequestFields(
                "anyURL",
                anotherSignUpRequest.getNickname(),
                anotherSignUpRequest.getPhoneNumber(),
                anotherSignUpRequest.getEmail(),
                anotherSignUpRequest.getRealName(),
                anotherSignUpRequest.getStatusMsg(),
                anotherSignUpRequest.getUsername(),
                Role.valueOf(anotherSignUpRequest.getRole().toUpperCase(Locale.ROOT))
        );
        userService.save(anotherUser);

        //When
        UserBriefInformationResponseDto foundUserInfo = userService.userBriefInformation("anotherTestUsername");

        //Then
        assertNotEquals(foundUserInfo.getUserId(), user.getId());
    }

}