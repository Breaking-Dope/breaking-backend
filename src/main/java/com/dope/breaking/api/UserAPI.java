package com.dope.breaking.api;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.PostResType;
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

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.security.Principal;
import java.util.*;

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
    public ResponseEntity<?> signInConfirm(
            @RequestPart String signUpRequest,
            @RequestPart (required = false) List<MultipartFile> profileImg) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

        //ObjectMapper의 readerFor는 @Valid가 적용되지 않습니다. 고로 validator로 추가 검증 절차가 필요합니다.
        SignUpRequestDto signUpRequestDto = mapper.readerFor(SignUpRequestDto.class).readValue(signUpRequest);

        //validator를 통해 User entity 중 @NotNull 조건은 만족하지 못하는 필드를 getPropertyPath()로 잡아냅니다.
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<SignUpRequestDto>> violations = validator.validate(signUpRequestDto);

        Map<String,String> nullFieldMap = new LinkedHashMap<>();
        for (ConstraintViolation<SignUpRequestDto> violation : violations) {
            nullFieldMap.put(String.valueOf(violation.getPropertyPath()),violation.getMessage());
        }

        //nullFieldList가 empty하지 않으면 있으면 안되는 null 값이 있다는 것입니다.
        //고로 이 nullFieldList를 담은 map을 body에 넣어 400 HttpStatus를 전송합니다.
        if(!nullFieldMap.isEmpty()){
            return ResponseEntity.badRequest().body(nullFieldMap);
        }

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
    public ResponseEntity<?> profileUpdateConfirm(
            Principal principal,
            @RequestPart String updateRequest,
            @RequestPart (required = false) List<MultipartFile> profileImg) throws Exception {


        // 1. check the username (not_found / not registered)
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponseDto(PostResType.NOT_FOUND_USER.getMessage()));
        }//유저 정보 없으면 일치하지 않다고 반환하기.
        if (!userService.existByUsername(principal.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDto(PostResType.NOT_REGISTERED_USER.getMessage()));
        }

        // 2. create UpdateRequestDto with ObjectMapper (AND validation check)
        ObjectMapper mapper = new ObjectMapper();
        UpdateRequestDto updateRequestDto = mapper.readerFor(UpdateRequestDto.class).readValue(updateRequest);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UpdateRequestDto>> violations = validator.validate(updateRequestDto);

        Map<String,String> nullFieldMap = new LinkedHashMap<>();

        for (ConstraintViolation<UpdateRequestDto> violation : violations) {
            nullFieldMap.put(String.valueOf(violation.getPropertyPath()),violation.getMessage());
        }

        if(!nullFieldMap.isEmpty()){
            return ResponseEntity.badRequest().body(nullFieldMap);
        }

        // 3. find the user by username.
        User user = userService.findByUsername(principal.getName()).get();

        // 4. validation (email, nickname, and phone number)
        String invalidMessage = userService.invalidMessage(updateRequestDto,user);

        if (!Objects.equals(invalidMessage, "")){

            return ResponseEntity.badRequest().body(new MessageResponseDto(invalidMessage));

        }

        // 5. update profile

        String profileImgFileName = mediaService.getBasicProfileDir();
        String originalProfileUrl = user.getProfileImgURL();

        // case 1: 기본 이미지 -> 기본 이미지 : 변경 없음

        // case 2: 유저 본인 선택 이미지 -> 기본 이미지
        if (originalProfileUrl != mediaService.getBasicProfileDir() && profileImg == null){
            File file = new File(mediaService.getDirName()+File.separator+originalProfileUrl);
            file.delete();
        }

        // case 3: 기본 이미지 -> 유저 본인 선택 이미지
        else if (originalProfileUrl == mediaService.getBasicProfileDir() && profileImg != null){
            profileImgFileName =  mediaService.uploadMedias(profileImg).get(0);
        }

        // case 4: 유저 본인 선택 이미지 -> 유저 본인 선택 이미지
        else if (originalProfileUrl != mediaService.getBasicProfileDir() && profileImg != null){
            File file = new File(mediaService.getDirName()+File.separator+originalProfileUrl);
            file.delete();
            profileImgFileName =  mediaService.uploadMedias(profileImg).get(0);
        }

        // 6. update the user information
        user.setRequestFields(
                profileImgFileName,
                updateRequestDto.getNickname(),
                updateRequestDto.getPhoneNumber(),
                updateRequestDto.getEmail(),
                updateRequestDto.getRealName(),
                updateRequestDto.getStatusMsg(),
                principal.getName(),
                Role.valueOf(updateRequestDto.getRole().toUpperCase(Locale.ROOT))
        );

        userService.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

}
