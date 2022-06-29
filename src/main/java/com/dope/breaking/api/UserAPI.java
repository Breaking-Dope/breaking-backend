package com.dope.breaking.api;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.*;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.service.UserService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import javax.validation.Valid;
import java.util.Optional;

@RestController
public class UserAPI {

    @Autowired
    public UserRepository userRepository;

    @PostMapping("/oauth2/sign-up/validate-phone-number")
    public ResponseEntity<Void> validatePhoneNumber(@RequestBody PhoneNumberValidateRequestDto phoneNumberValidateRequest){

        Optional<User> user =  userRepository.findByPhoneNumber(phoneNumberValidateRequest.getPhoneNumber());

        if (user.isPresent()){
            return ResponseEntity.badRequest().build();
        }

        else{
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/oauth2/sign-up/validate-email")
    public ResponseEntity<MessageResponseDto> validateEmail(@RequestBody EmailValidateRequestDto emailValidateRequest){

        if(!UserService.isValidEmail(emailValidateRequest.getEmail())){
            return ResponseEntity.badRequest().body(new MessageResponseDto("invalid email"));
        }

        Optional<User> user = userRepository.findByEmail(emailValidateRequest.getEmail());

        if (user.isPresent()){
            return ResponseEntity.badRequest().build();
        }

        else{
            return ResponseEntity.ok().build();
        }

    }

    @PostMapping("/oauth2/sign-up/validate-nickname")
    public ResponseEntity<Void> validateNickname(@RequestBody NicknameValidateRequestDto nicknameValidateRequest){

        Optional<User> user = userRepository.findByNickname(nicknameValidateRequest.getNickname());

        if (user.isPresent()){
            return ResponseEntity.badRequest().build();
        }

        else{
            return ResponseEntity.ok().build();
        }

    }

    @PostMapping("/oauth2/sign-up")
    public ResponseEntity<MessageResponseDto> signInConfirm(@RequestBody @Valid SignUpRequestDto signUpRequest){

        if(!UserService.isValidEmail(signUpRequest.getEmail())){
            return ResponseEntity.badRequest().body(new MessageResponseDto("invalid email"));
        }

        if (userRepository.findByPhoneNumber(signUpRequest.getPhoneNumber()).isPresent()){
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDto(SignUpDuplicateType.PHONE_NUMBER_DUPLICATE.getMessage()));
        }

        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()){
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDto(SignUpDuplicateType.EMAIL_DUPLICATE.getMessage()));
        }

        if (userRepository.findByNickname(signUpRequest.getNickname()).isPresent()){
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDto(SignUpDuplicateType.NICKNAME_DUPLICATE.getMessage()));
        }

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

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }


    @Data
    @NoArgsConstructor
    public static class PhoneNumberRequest{

        private String phoneNumber;

    }



}
