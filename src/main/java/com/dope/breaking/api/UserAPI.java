package com.dope.breaking.api;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.response.MessageResponseDto;
import com.dope.breaking.dto.user.*;
import com.dope.breaking.service.MediaService;
import com.dope.breaking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserAPI {

    private final UserService userService;
    private final MediaService mediaService;

    @PostMapping("/oauth2/sign-up/validate-phone-number")
    public ResponseEntity<MessageResponseDto> validatePhoneNumber(@RequestBody PhoneNumberValidateRequestDto phoneNumberValidateRequest){

        if(!UserService.isValidPhoneNumberFormat(phoneNumberValidateRequest.getPhoneNumber())){
            return ResponseEntity.badRequest().body(new MessageResponseDto(SignUpErrorType.INVALID_PHONE_NUMBER.getMessage()));
        }

        Optional<User> user =  userService.findByPhoneNumber(phoneNumberValidateRequest.getPhoneNumber());

        if (user.isPresent()){
            return ResponseEntity.badRequest().body(new MessageResponseDto(SignUpErrorType.PHONE_NUMBER_DUPLICATE.getMessage()));
        }
        else{
            return ResponseEntity.ok().build();
        }

    }

    @PostMapping("/oauth2/sign-up/validate-email")
    public ResponseEntity<MessageResponseDto> validateEmail(@RequestBody EmailValidateRequestDto emailValidateRequest){

        if(!UserService.isValidEmailFormat(emailValidateRequest.getEmail())){
            return ResponseEntity.badRequest().body(new MessageResponseDto(SignUpErrorType.INVALID_EMAIL.getMessage()));
        }

        Optional<User> user = userService.findByEmail(emailValidateRequest.getEmail());

        if (user.isPresent()){
            return ResponseEntity.badRequest().body(new MessageResponseDto(SignUpErrorType.EMAIL_DUPLICATE.getMessage()));
        }
        else{
            return ResponseEntity.ok().build();
        }

    }

    @PostMapping("/oauth2/sign-up/validate-nickname")
    public ResponseEntity<MessageResponseDto> validateNickname(@RequestBody NicknameValidateRequestDto nicknameValidateRequest){

        Optional<User> user = userService.findByNickname(nicknameValidateRequest.getNickname());

        if (user.isPresent()){
            return ResponseEntity.badRequest().body(new MessageResponseDto(SignUpErrorType.NICKNAME_DUPLICATE.getMessage()));
        }
        else{
            return ResponseEntity.ok().build();
        }

    }

//    @PutMapping("/profile/{userId}",consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
//    public void updateProfile(
//            @PathVariable(value = "userId") String userId,
//            @RequestPart @Valid SignUpRequestDto signUpRequestDto,
//            @RequestPart (required = false) List<MultipartFile> profileImg) throws Exception{
//
//
//
//    }

    @PostMapping(value = "/oauth2/sign-up",consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<MessageResponseDto> signInConfirm(
            @RequestPart @Valid SignUpRequestDto signUpRequest,
            @RequestPart (required = false) List<MultipartFile> profileImg) throws Exception {
        
        String invalidateMessage = userService.invalidMessage(signUpRequest);

        if (invalidateMessage != ""){
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDto(invalidateMessage));
        }

        String profileImgFileName = mediaService.getBasicProfileDir();

        if (profileImg != null){
            profileImgFileName =  mediaService.uploadMedias(profileImg).get(0);
        }

        User user = new User();

        user.signUp(
                profileImgFileName,
                signUpRequest.getNickname(),
                signUpRequest.getPhoneNumber(),
                signUpRequest.getEmail(),
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getStatusMsg(),
                signUpRequest.getUsername(),
                Role.valueOf(signUpRequest.getRole().toUpperCase(Locale.ROOT))
        );

        userService.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

}
