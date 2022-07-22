package com.dope.breaking.service;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.FullUserInformationResponse;
import com.dope.breaking.dto.user.ProfileInformationResponseDto;
import com.dope.breaking.dto.user.SignUpRequestDto;
import com.dope.breaking.dto.user.UserBriefInformationResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.user.DuplicatedUserInformationException;
import com.dope.breaking.exception.user.InvalidUserInformationFormatException;
import com.dope.breaking.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired UserService userService;
    @Autowired UserRepository userRepository;
    @Autowired EntityManager em;


    @Test
    void validateNickname() {

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
                "anyURL",
                signUpRequest.getNickname(),
                signUpRequest.getPhoneNumber(),
                signUpRequest.getEmail(),
                signUpRequest.getRealName(),
                signUpRequest.getStatusMsg(),
                signUpRequest.getUsername(),
                Role.valueOf(signUpRequest.getRole().toUpperCase(Locale.ROOT))

        );

        User savedUser = userRepository.save(user);

        // Then
        User foundUser = userRepository.findById(savedUser.getId()).get();
        assertEquals(foundUser, user);

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

        //When
        UserBriefInformationResponseDto foundUserInfo = userService.userBriefInformation("testUsername");

        //Then
        assertEquals(foundUserInfo.getUserId(), user.getId());
        assertEquals(foundUserInfo.getNickname(), user.getNickname());
        assertEquals(foundUserInfo.getProfileImgURL(), user.getOriginalProfileImgURL());
    }

    @Test
    void validateJwtTokenFailureNotFoundUserName() {

        //Given
        User user = new User();
        SignUpRequestDto signUpRequest =  new SignUpRequestDto
                ("statusMsg","nickname","phoneNumber","mwk300@nyu.edu","realname","testUsername", "press");
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

        User anotherUser = new User();
        SignUpRequestDto anotherSignUpRequest =  new SignUpRequestDto
                ("statusMsg","nickname","phoneNumber","mwk300@nyu.edu","realname","anotherTestUsername", "press");
        anotherUser.setRequestFields(
                "anyURL",
                "anyURL",
                anotherSignUpRequest.getNickname(),
                anotherSignUpRequest.getPhoneNumber(),
                anotherSignUpRequest.getEmail(),
                anotherSignUpRequest.getRealName(),
                anotherSignUpRequest.getStatusMsg(),
                anotherSignUpRequest.getUsername(),
                Role.valueOf(anotherSignUpRequest.getRole().toUpperCase(Locale.ROOT))
        );
        userRepository.save(anotherUser);

        //When
        UserBriefInformationResponseDto foundUserInfo = userService.userBriefInformation("anotherTestUsername");

        //Then
        assertNotEquals(foundUserInfo.getUserId(), user.getId());
    }

    @Test
    void getUserInformationInProfilePage() {

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

        ProfileInformationResponseDto profileInformationResponseDto = userService.profileInformation(null, user.getId());

        assertEquals(profileInformationResponseDto.getEmail(), user.getEmail());
        assertEquals(profileInformationResponseDto.getNickname(), user.getNickname());

    }

    @Test
    void getFullUserInformation() {

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

        FullUserInformationResponse fullUserInformationResponse = userService.getFullUserInformation(user.getUsername());

        assertEquals(fullUserInformationResponse.getPhoneNumber(), user.getPhoneNumber());
        assertEquals(fullUserInformationResponse.getRealName(), user.getRealName());
    }

    @DisplayName("휴대폰 번호 형식이 잘못되었을 경우, 예외가 발생한다.")
    @Test()
    void validateUserPhoneNumber() {

        userService.validatePhoneNumber("01026695282", null);
        Assertions.assertThrows(InvalidUserInformationFormatException.class, () -> userService.validatePhoneNumber("0102669528", null));
        Assertions.assertThrows(InvalidUserInformationFormatException.class, () -> userService.validatePhoneNumber("010-2669-5282", null));
        Assertions.assertThrows(InvalidUserInformationFormatException.class, () -> userService.validatePhoneNumber("aaaaaaaaaaa", null));
    }

    @DisplayName("휴대폰 번호 중복이 있을 경우, 예외가 발생한다.")
    @Test
    void duplicatedUserPhoneNumberFailure() {

        User oldUser = new User();
        SignUpRequestDto signUpRequest =  new SignUpRequestDto
                ("statusMsg","nickname","01026695282","test@email.com","realname","testUsername", "press");
        oldUser.setRequestFields(
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
        userRepository.save(oldUser);
        Assertions.assertThrows(DuplicatedUserInformationException.class, () -> userService.validatePhoneNumber(oldUser.getPhoneNumber(), null));
    }

    @DisplayName("이메일 형식이 잘못되었을 경우, 예외가 발생한다.")
    @Test
    void validateUserEmail() {

        userService.validateEmail("woojin8787@gmail.com", null);
        Assertions.assertThrows(InvalidUserInformationFormatException.class, () -> userService.validateEmail("woojin8787", null));
        Assertions.assertThrows(InvalidUserInformationFormatException.class, () -> userService.validateEmail("woojin8787gmail.com", null));
        Assertions.assertThrows(InvalidUserInformationFormatException.class, () -> userService.validateEmail("woojin8787@gmail", null));
    }

    @DisplayName("이메일 중복이 있을 경우, 예외가 발생한다.")
    @Test
    void duplicatedUserEmailFailure() {

        User oldUser = new User();
        SignUpRequestDto signUpRequest =  new SignUpRequestDto
                ("statusMsg","nickname","01026695282","test@email.com","realname","testUsername", "press");
        oldUser.setRequestFields(
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
        userRepository.save(oldUser);
        Assertions.assertThrows(DuplicatedUserInformationException.class, () -> userService.validateEmail(oldUser.getEmail(), null));
    }

    @DisplayName("닉네임 형식이 잘못되었을 경우, 예외가 발생한다.")
    @Test
    void validateUserNicknameFormat() {

        userService.validateNickname("AbCdEf", null);
        userService.validateNickname("abc123", null);
        Assertions.assertThrows(InvalidUserInformationFormatException.class, () -> userService.validateNickname("@ABC", null));
        Assertions.assertThrows(InvalidUserInformationFormatException.class, () -> userService.validateNickname("b", null));
    }

    @DisplayName("휴대폰 번호 중복이 있을 경우, 예외가 발생한다.")
    @Test
    void duplicatedUserNicknameFailure() {

        User oldUser = new User();
        SignUpRequestDto signUpRequest =  new SignUpRequestDto
                ("statusMsg","nickname","phoneNumber","test@email.com","realname","testUsername", "press");
        oldUser.setRequestFields(
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
        userRepository.save(oldUser);
        Assertions.assertThrows(DuplicatedUserInformationException.class, () -> userService.validateNickname(oldUser.getNickname(), null));
    }

    @DisplayName("회원 수정에서, 기존 회원이 같은 정보를 입력하면 중복 예외가 발생하지 않는다.")
    @Test
    void validateWhenCurrentUserUpdateProfileWithSameInformation() {

        User currentUser = new User();
        SignUpRequestDto signUpRequest =  new SignUpRequestDto
                ("statusMsg","nickname","01026695282","test@email.com","realname","testUsername", "press");
        currentUser.setRequestFields(
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

        userRepository.save(currentUser);

        userService.validatePhoneNumber(currentUser.getPhoneNumber(), currentUser.getUsername());
        userService.validateEmail(currentUser.getEmail(), currentUser.getUsername());
        userService.validateNickname(currentUser.getNickname(), currentUser.getUsername());
    }

}