package com.dope.breaking.api;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.SignUpRequest;
import com.dope.breaking.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserAPITest {

    @Autowired UserRepository userRepository;

    @Test
    void signInConfirm() {

        // Given
        SignUpRequest signUpRequest =  new SignUpRequest
                ("URL","nickname","phoneNumber",
                        "mwk300@nyu.edu","Minwu","Kim","msg");

        // When
        User user = new User();

        user.signUp(
                signUpRequest.getProfileImgURL(),
                signUpRequest.getNickname(),
                signUpRequest.getPhoneNumber(),
                signUpRequest.getEmail(),
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getStatusMsg()
        );

        User savedUser = userRepository.save(user);

        // Then
        User foundUser = userRepository.findById(savedUser.getId()).get();
        Assertions.assertThat(foundUser).isEqualTo(user);

    }

}