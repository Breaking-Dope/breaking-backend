package com.dope.breaking.api;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.response.MessageResponseDto;
import com.dope.breaking.dto.user.*;
import com.dope.breaking.service.MediaService;
import com.dope.breaking.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
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

        Optional<User> user = userService.findByPhoneNumber(phoneNumberValidateRequest.getPhoneNumber());

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

    @PostMapping(value = "/oauth2/sign-up", consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<MessageResponseDto> signInConfirm(
            @RequestPart String signUpRequest,
            @RequestPart (required = false) List<MultipartFile> profileImg) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        SignUpRequestDto signUpRequestDto = mapper.readValue(signUpRequest,SignUpRequestDto.class);

        String invalidMessage = userService.invalidMessage(signUpRequestDto);

        if (invalidMessage != ""){
            return ResponseEntity.badRequest().body(new MessageResponseDto(invalidMessage));
        }

        String profileImgFileName = mediaService.getBasicProfileDir();

        if (profileImg != null){
            profileImgFileName =  mediaService.uploadMedias(profileImg).get(0);
        }

        User user = new User();
        user.setRequestFields(
                profileImgFileName,
                signUpRequestDto.getNickname(),
                signUpRequestDto.getPhoneNumber(),
                signUpRequestDto.getEmail(),
                signUpRequestDto.getRealName(),
                signUpRequestDto.getStatusMsg(),
                signUpRequestDto.getUsername(),
                Role.valueOf(signUpRequestDto.getRole().toUpperCase(Locale.ROOT))
        );

        userService.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/profile", consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<MessageResponseDto> profileUpdateConfirm(
            Principal principal,
            @RequestPart String signUpRequest,
            @RequestPart (required = false) List<MultipartFile> profileImg) throws Exception {

        Optional<String> cntUsername = Optional.ofNullable(principal.getName());

        if (cntUsername.isEmpty()) {
            return ResponseEntity.status(401).body(new MessageResponseDto(SignUpErrorType.NOT_FOUND_USER.getMessage()));
        }

        if (!userService.existByUsername(cntUsername.get())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(SignUpErrorType.NOT_REGISTERED_USER.getMessage()));
        }

        User user = userService.findByUsername(cntUsername.get()).get();

        ObjectMapper mapper = new ObjectMapper();
        SignUpRequestDto signUpRequestDto = mapper.readValue(signUpRequest,SignUpRequestDto.class);

        String invalidMessage = userService.invalidMessage(signUpRequestDto);

        if (invalidMessage != ""){
            if(user.getPhoneNumber() != signUpRequestDto.getPhoneNumber()
                && user.getNickname() != signUpRequestDto.getNickname()
                && user.getEmail() != signUpRequestDto.getEmail()){

                return ResponseEntity.badRequest().body(new MessageResponseDto(invalidMessage));

            }

        }

        String profileImgFileName = mediaService.getBasicProfileDir();

        if (profileImg != null){
            profileImgFileName =  mediaService.uploadMedias(profileImg).get(0);
        }

        user.setRequestFields(
                profileImgFileName,
                signUpRequestDto.getNickname(),
                signUpRequestDto.getPhoneNumber(),
                signUpRequestDto.getEmail(),
                signUpRequestDto.getRealName(),
                signUpRequestDto.getStatusMsg(),
                signUpRequestDto.getUsername(),
                Role.valueOf(signUpRequestDto.getRole().toUpperCase(Locale.ROOT))
        );

        userService.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

}
