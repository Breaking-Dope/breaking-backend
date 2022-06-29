package com.dope.breaking.api;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.*;
import com.dope.breaking.exception.SignInException;
import com.dope.breaking.repository.UserRepository;
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
    public ResponseEntity<Void> validatePhoneNumber(@RequestBody PhoneNumberValidateRequest phoneNumberValidateRequest){

        Optional<User> user =  userRepository.findByPhoneNumber(phoneNumberValidateRequest.getPhoneNumber());

        if (user.isPresent()){
            return ResponseEntity.badRequest().build();
        }

        else{
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/oauth2/sign-up/validate-email")
    public ResponseEntity<MessageResponse> validateEmail(@RequestBody EmailValidateRequest emailValidateRequest){

        if(!SignInException.isValidEmail(emailValidateRequest.getEmail())){
            return ResponseEntity.badRequest().body(new MessageResponse("invalid email"));
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
    public ResponseEntity<Void> validateNickname(@RequestBody NicknameValidateRequest nicknameValidateRequest){

        Optional<User> user = userRepository.findByNickname(nicknameValidateRequest.getNickname());

        if (user.isPresent()){
            return ResponseEntity.badRequest().build();
        }

        else{
            return ResponseEntity.ok().build();
        }

    }

    @PostMapping("/oauth2/sign-up")
    public ResponseEntity<MessageResponse> signInConfirm(@RequestBody @Valid SignUpRequest signUpRequest){

        if(!SignInException.isValidEmail(signUpRequest.getEmail())){
            return ResponseEntity.badRequest().body(new MessageResponse("invalid email"));
        }

        if (userRepository.findByPhoneNumber(signUpRequest.getPhoneNumber()).isPresent()){
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(SignUpDuplicateType.PHONE_NUMBER_DUPLICATE.getMessage()));
        }

        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()){
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(SignUpDuplicateType.EMAIL_DUPLICATE.getMessage()));
        }

        if (userRepository.findByNickname(signUpRequest.getNickname()).isPresent()){
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(SignUpDuplicateType.NICKNAME_DUPLICATE.getMessage()));
        }

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

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }


    @Data
    @NoArgsConstructor
    public static class PhoneNumberRequest{

        private String phoneNumber;

    }



}
