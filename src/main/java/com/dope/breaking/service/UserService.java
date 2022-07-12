package com.dope.breaking.service;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.SignUpRequestDto;
import com.dope.breaking.dto.user.UpdateUserRequestDto;
import com.dope.breaking.dto.user.UserBriefInformationResponseDto;
import com.dope.breaking.exception.CustomInternalErrorException;
import com.dope.breaking.exception.auth.DuplicatedInformationException;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.auth.invalidUserInformationFormatException;
import com.dope.breaking.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final MediaService mediaService;

    public String signUp(String signUpRequest, List<MultipartFile> profileImg) {

        SignUpRequestDto signUpRequestDto = transformUserInformationToObject(signUpRequest);

        validateUserInformation(signUpRequestDto);

        String profileImgURL = mediaService.getBasicProfileDir();

        if (profileImg != null){
            profileImgURL =  mediaService.uploadMedias(profileImg).get(0);
        }

        User user = new User();
        user.setRequestFields(
                profileImgURL,
                signUpRequestDto.getNickname(),
                signUpRequestDto.getPhoneNumber(),
                signUpRequestDto.getEmail(),
                signUpRequestDto.getRealName(),
                signUpRequestDto.getStatusMsg(),
                signUpRequestDto.getUsername(),
                Role.valueOf(signUpRequestDto.getRole().toUpperCase(Locale.ROOT))
        );

        userRepository.save(user);
        return user.getUsername();
        
    }

    public void updateProfile(String username, String updateRequestDto, List<MultipartFile> profileImg) {

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);

        UpdateUserRequestDto updateUserRequestDto = transformUserInformationToObject(updateRequestDto, username);

        validateUserInformation(updateUserRequestDto, user);

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
                updateUserRequestDto.getNickname(),
                updateUserRequestDto.getPhoneNumber(),
                updateUserRequestDto.getEmail(),
                updateUserRequestDto.getRealName(),
                updateUserRequestDto.getStatusMsg(),
                updateUserRequestDto.getUsername(),
                Role.valueOf(updateUserRequestDto.getRole().toUpperCase(Locale.ROOT))
        );

        userRepository.save(user);

    }

    public UserBriefInformationResponseDto userBriefInformation(String username) {

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        return new UserBriefInformationResponseDto(user.getProfileImgURL(), user.getNickname(), user.getId());
    }

    private SignUpRequestDto transformUserInformationToObject(String signUpRequest) {

        ObjectMapper mapper = new ObjectMapper();
        SignUpRequestDto signUpRequestDto;

        //String 으로 되어있는 객체를 변환
        try {
            signUpRequestDto = mapper.readerFor(SignUpRequestDto.class).readValue(signUpRequest);
        } catch(Exception e) {
            throw new CustomInternalErrorException(e.getMessage());
        }

        return signUpRequestDto;
    }

    private UpdateUserRequestDto transformUserInformationToObject(String signUpRequest, String username) {

        ObjectMapper mapper = new ObjectMapper();
        UpdateUserRequestDto updateUserRequestDto;

        //String 으로 되어있는 객체를 변환
        try {
            updateUserRequestDto = mapper.readerFor(SignUpRequestDto.class).readValue(signUpRequest);
        } catch(Exception e) {
            throw new CustomInternalErrorException(e.getMessage());
        }

        updateUserRequestDto.setUsername(username);

        return updateUserRequestDto;
    }

    public void validateUserInformation (SignUpRequestDto signUpRequestDto){

        //null 체크
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<SignUpRequestDto>> violations = validator.validate(signUpRequestDto);

        for(ConstraintViolation<SignUpRequestDto> violation : violations) {
            throw new MissingFormatArgumentException(String.valueOf(violation.getPropertyPath()));
        }

        validatePhoneNumber(signUpRequestDto.getPhoneNumber());
        validateEmail(signUpRequestDto.getEmail());
        validateNickname(signUpRequestDto.getNickname());
        validateRole(signUpRequestDto.getRole());

    }

    public void validateUserInformation (UpdateUserRequestDto updateUserRequestDto, User preUserInformation){

        //null 체크
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UpdateUserRequestDto>> violations = validator.validate(updateUserRequestDto);

        for(ConstraintViolation<UpdateUserRequestDto> violation : violations) {
            throw new MissingFormatArgumentException(String.valueOf(violation.getPropertyPath()));
        }

        if(!updateUserRequestDto.getPhoneNumber().equals(preUserInformation.getPhoneNumber())) {
            validatePhoneNumber(updateUserRequestDto.getPhoneNumber());
        }
        if(!updateUserRequestDto.getEmail().equals(preUserInformation.getEmail())) {
            validateEmail(updateUserRequestDto.getEmail());
        }
        if(!updateUserRequestDto.getNickname().equals(preUserInformation.getNickname())) {
            validateNickname(updateUserRequestDto.getNickname());
        }
        validateRole(updateUserRequestDto.getRole());

    }

    public void validatePhoneNumber(String phoneNumber) {

        if(!Pattern.matches("^\\d{2,3}-\\d{3,4}-\\d{4}$", phoneNumber)){
            throw new invalidUserInformationFormatException(FailableUserInformation.PHONENUMBER);
        }

        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        if (user.isPresent()){
            throw new DuplicatedInformationException(FailableUserInformation.PHONENUMBER);
        }
    }

    public void validateEmail(String email) {

        if(!Pattern.matches("^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$", email)){
            throw new invalidUserInformationFormatException(FailableUserInformation.EMAIL);
        }

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()){
            throw new DuplicatedInformationException(FailableUserInformation.EMAIL);
        }
    }

    public void validateNickname(String nickname) {

        if(!Pattern.matches("^(?=.*[a-z0-9가-힣])[a-z0-9가-힣]{2,16}$", nickname)){
            throw new invalidUserInformationFormatException(FailableUserInformation.NICKNAME);
        }

        Optional<User> user = userRepository.findByNickname(nickname);
        if (user.isPresent()){
            throw new DuplicatedInformationException(FailableUserInformation.NICKNAME);
        }
    }

    public static void validateRole(String role) {

        String upperCasedRole = role.toUpperCase();

        if(upperCasedRole.equals("PRESS") || upperCasedRole.equals("USER")) {
            throw new invalidUserInformationFormatException(FailableUserInformation.ROLE);
        }

    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id){
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Boolean existByUsername(String username){
        return userRepository.existsByUsername(username);
    }

    public Boolean existById(Long userId){
        return userRepository.existsById(userId);
    }

    
}
