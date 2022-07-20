package com.dope.breaking.service;

import com.dope.breaking.domain.media.UploadType;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.*;
import com.dope.breaking.exception.CustomInternalErrorException;
import com.dope.breaking.exception.NotValidRequestBodyException;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.user.DuplicatedUserInformationException;
import com.dope.breaking.exception.user.InvalidUserInformationFormatException;
import com.dope.breaking.exception.user.NoSuchUserException;
import com.dope.breaking.repository.FollowRepository;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

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
    private final FollowRepository followRepository;
    private final FollowService followService;

    private final JwtTokenProvider jwtTokenProvider;

    public ResponseEntity<?> signUp(String signUpRequest, List<MultipartFile> profileImg) {

        SignUpRequestDto signUpRequestDto = transformUserInformationToObject(signUpRequest);

        validateUserInformation(signUpRequestDto);

        String profileImgURL = mediaService.getBasicProfileDir();

        if (profileImg != null) {
            profileImgURL = mediaService.uploadMedias(profileImg, UploadType.ORIGNAL_PROFILE_IMG).get(0);
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

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", jwtTokenProvider.createAccessToken(user.getUsername()));
        String refreshjwt = jwtTokenProvider.createRefreshToken();
        user.updateRefreshToken(refreshjwt);
        httpHeaders.set("Authorization-refresh", refreshjwt);
        UserBriefInformationResponseDto userBriefInformationResponseDto = UserBriefInformationResponseDto.builder()
                .balance(user.getBalance())
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImgURL(user.getOriginalProfileImgURL())
                .build();

        return new ResponseEntity<UserBriefInformationResponseDto>(userBriefInformationResponseDto, httpHeaders, HttpStatus.CREATED);
    }

    public void updateProfile(String username, String updateRequestDto, List<MultipartFile> profileImg) {

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);

        UpdateUserRequestDto updateUserRequestDto = transformUserInformationToObject(updateRequestDto, username);

        validateUserInformation(updateUserRequestDto, user);

        // 5. update profile

        String profileImgFileName = mediaService.getBasicProfileDir();
        String originalProfileUrl = user.getOriginalProfileImgURL();

        // case 1: 기본 이미지 -> 기본 이미지 : 변경 없음

        // case 2: 유저 본인 선택 이미지 -> 기본 이미지
        if (originalProfileUrl != mediaService.getBasicProfileDir() && profileImg == null) {
            File file = new File(mediaService.getMAIN_DIR_NAME() + File.separator + originalProfileUrl);
            file.delete();
        }

        // case 3: 기본 이미지 -> 유저 본인 선택 이미지
        else if (originalProfileUrl == mediaService.getBasicProfileDir() && profileImg != null) {
            profileImgFileName = mediaService.uploadMedias(profileImg, UploadType.ORIGNAL_PROFILE_IMG).get(0);
        }

        // case 4: 유저 본인 선택 이미지 -> 유저 본인 선택 이미지
        else if (originalProfileUrl != mediaService.getBasicProfileDir() && profileImg != null) {
            File file = new File(mediaService.getMAIN_DIR_NAME() + File.separator + originalProfileUrl);
            file.delete();
            profileImgFileName = mediaService.uploadMedias(profileImg, UploadType.ORIGNAL_PROFILE_IMG).get(0);
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
        return new UserBriefInformationResponseDto(user.getOriginalProfileImgURL(), user.getNickname(), user.getId(), user.getBalance());
    }

    private SignUpRequestDto transformUserInformationToObject(String signUpRequest) {

        ObjectMapper mapper = new ObjectMapper();
        SignUpRequestDto signUpRequestDto;

        //String 으로 되어있는 객체를 변환
        try {
            signUpRequestDto = mapper.readerFor(SignUpRequestDto.class).readValue(signUpRequest);
        } catch (Exception e) {
            throw new CustomInternalErrorException(e.getMessage());
        }

        return signUpRequestDto;
    }

    private UpdateUserRequestDto transformUserInformationToObject(String updateUserRequest, String username) {

        ObjectMapper mapper = new ObjectMapper();
        UpdateUserRequestDto updateUserRequestDto;

        //String 으로 되어있는 객체를 변환
        try {
            updateUserRequestDto = mapper.readerFor(UpdateUserRequestDto.class).readValue(updateUserRequest);
        } catch(Exception e) {
            throw new CustomInternalErrorException(e.getMessage());
        }

        updateUserRequestDto.setUsername(username);

        return updateUserRequestDto;
    }

    public void validateUserInformation(SignUpRequestDto signUpRequestDto) {

        //null 체크
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<SignUpRequestDto>> violations = validator.validate(signUpRequestDto);

        for (ConstraintViolation<SignUpRequestDto> violation : violations) {
            throw new MissingFormatArgumentException(String.valueOf(violation.getPropertyPath()));
        }

        validatePhoneNumber(signUpRequestDto.getPhoneNumber());
        validateEmail(signUpRequestDto.getEmail());
        validateNickname(signUpRequestDto.getNickname());
        validateRole(signUpRequestDto.getRole());

    }

    public void validateUserInformation(UpdateUserRequestDto updateUserRequestDto, User preUserInformation) {

        //null 체크
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UpdateUserRequestDto>> violations = validator.validate(updateUserRequestDto);


        if (violations.size() > 0) {
            StringBuffer result = new StringBuffer();

            for (ConstraintViolation<UpdateUserRequestDto> violation : violations) {
                result.append(String.valueOf(violation.getPropertyPath())).append(", ");
            }

            result.delete(result.length() - 2, result.length());
            log.info(result.toString());

            throw new NotValidRequestBodyException(result.toString());
        }

        if (!updateUserRequestDto.getPhoneNumber().equals(preUserInformation.getPhoneNumber())) {
            validatePhoneNumber(updateUserRequestDto.getPhoneNumber());
        }
        if (!updateUserRequestDto.getEmail().equals(preUserInformation.getEmail())) {
            validateEmail(updateUserRequestDto.getEmail());
        }
        if (!updateUserRequestDto.getNickname().equals(preUserInformation.getNickname())) {
            validateNickname(updateUserRequestDto.getNickname());
        }
        validateRole(updateUserRequestDto.getRole());

    }

    public void validatePhoneNumber(String phoneNumber) {

        if (!Pattern.matches("^(\\d{11}|\\d{3}\\d{4}\\d{4})$", phoneNumber)) {
            throw new InvalidUserInformationFormatException(FailableUserInformation.PHONENUMBER);
        }

        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        if (user.isPresent()) {
            throw new DuplicatedUserInformationException(FailableUserInformation.PHONENUMBER);
        }
    }

    public void validateEmail(String email) {

        if (!Pattern.matches("^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$", email)) {
            throw new InvalidUserInformationFormatException(FailableUserInformation.EMAIL);
        }

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            throw new DuplicatedUserInformationException(FailableUserInformation.EMAIL);
        }
    }

    public void validateNickname(String nickname) {

        if (!Pattern.matches("^(?=.*[a-z0-9가-힣])[a-z0-9가-힣]{2,16}$", nickname)) {
            throw new InvalidUserInformationFormatException(FailableUserInformation.NICKNAME);
        }

        Optional<User> user = userRepository.findByNickname(nickname);
        if (user.isPresent()) {
            throw new DuplicatedUserInformationException(FailableUserInformation.NICKNAME);
        }
    }

    public static void validateRole(String role) {

        String upperCasedRole = role.toUpperCase();

        if (!(upperCasedRole.equals("PRESS") || upperCasedRole.equals("USER"))) {
            throw new InvalidUserInformationFormatException(FailableUserInformation.ROLE);
        }

    }


    public Optional<User> findByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }

    @Transactional
    public void setRefreshToken(String username, String refreshToken) {
        User user = userRepository.findByUsername(username).get();
        user.updateRefreshToken(refreshToken);
    }

    public ProfileInformationResponseDto profileInformation(String username, Long userId) {

        User user = userRepository.findById(userId).orElseThrow(NoSuchUserException::new);
        int followerCount = followRepository.countFollowsByFollowed(user);
        int followingCount = followRepository.countFollowsByFollowing(user);

        boolean isFollowing = false;
        if(username != null) {
            User me = userRepository.findByUsername(username).orElseThrow();
            isFollowing = followRepository.existsFollowsByFollowedAndFollowing(user, me);
        }

        return ProfileInformationResponseDto.builder()
                .userId(user.getId())
                .profileImgURL(user.getOriginalProfileImgURL())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .statusMsg(user.getStatusMsg())
                .role(user.getRole())
                .followerCount(followerCount)
                .followingCount(followingCount)
                .isFollowing(isFollowing)
                .build();

    }

    public FullUserInformationResponse getFullUserInformation(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        return FullUserInformationResponse.builder()
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .realName(user.getRealName())
                .role(user.getRole())
                .statusMsg(user.getStatusMsg())
                .profileImgURL(user.getOriginalProfileImgURL())
                .build();
    }
}
