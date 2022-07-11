package com.dope.breaking.api;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.PostResType;
import com.dope.breaking.dto.response.MessageResponseDto;
import com.dope.breaking.dto.user.*;
import com.dope.breaking.security.jwt.JwtTokenProvider;
import com.dope.breaking.service.MediaService;
import com.dope.breaking.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
    private final JwtTokenProvider jwtTokenProvider;


    @GetMapping("/oauth2/sign-up/validate-phone-number/{phoneNumber}")
    public ResponseEntity<Void> validatePhoneNumber(@PathVariable String phoneNumber){

        userService.validatePhoneNumber(phoneNumber);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/oauth2/sign-up/validate-email/{email}")
    public ResponseEntity<Void> validateEmail(@PathVariable String email){

        userService.validateEmail(email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/oauth2/sign-up/validate-nickname/{nickname}")
    public ResponseEntity<Void> validateNickname(@PathVariable String nickname){

        userService.validateNickname(nickname);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/oauth2/sign-up", consumes = {MediaType.TEXT_PLAIN_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> signUp(
            @RequestPart String signUpRequest,
            @RequestPart (required = false) List<MultipartFile> profileImg) {

        String username = userService.signUp(signUpRequest, profileImg);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", jwtTokenProvider.createAccessToken(username));
        return ResponseEntity.status(HttpStatus.CREATED).headers(httpHeaders).build();

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/oauth2/validate-jwt")
    public ResponseEntity<UserBriefInformationResponseDto> validateJwt(Principal principal) {
        return ResponseEntity.ok().body(userService.userBriefInformation(principal.getName()));
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
