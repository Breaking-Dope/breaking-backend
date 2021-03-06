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

        String originalProfileImgURL = null;
        String compressedProfileImgURL = null;


        if (profileImg != null) {
            originalProfileImgURL = mediaService.uploadMedias(profileImg, UploadType.ORIGNAL_PROFILE_IMG).get(0);
            compressedProfileImgURL = mediaService.compressImage(originalProfileImgURL);
        }

        User user = new User();
        user.setRequestFields(
                originalProfileImgURL,
                compressedProfileImgURL,
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

        String toSetOriginalProfileImgURL = null;
        String toSetCompressedProfileImgURL = null;

        String currentOriginalProfileImgUrl = user.getOriginalProfileImgURL();
        String currentCompressedProfileImgURL = user.getCompressedProfileImgURL();


        if(updateUserRequestDto.getIsProfileImgChanged()) {

            // case 1: ?????? ?????? ?????? ????????? -> ?????? ?????????
            if (currentOriginalProfileImgUrl != null && profileImg == null) {
                File file1 = new File(mediaService.getMAIN_DIR_NAME() + currentOriginalProfileImgUrl);
                File file2 = new File(mediaService.getMAIN_DIR_NAME() + currentCompressedProfileImgURL);
                file1.delete();
                file2.delete();
            }

            // case 2: ?????? ????????? -> ?????? ?????? ?????? ?????????
            else if (currentOriginalProfileImgUrl == null && profileImg != null) {
                toSetOriginalProfileImgURL = mediaService.uploadMedias(profileImg, UploadType.ORIGNAL_PROFILE_IMG).get(0);
                toSetCompressedProfileImgURL = mediaService.compressImage(toSetOriginalProfileImgURL);
            }

            // case 3: ?????? ?????? ?????? ????????? -> ?????? ?????? ?????? ?????????
            else if (currentOriginalProfileImgUrl != null && profileImg != null) {
                File file1 = new File(mediaService.getMAIN_DIR_NAME() + currentOriginalProfileImgUrl);
                File file2 = new File(mediaService.getMAIN_DIR_NAME() + currentCompressedProfileImgURL);
                file1.delete();
                file2.delete();

                toSetOriginalProfileImgURL = mediaService.uploadMedias(profileImg, UploadType.ORIGNAL_PROFILE_IMG).get(0);
                toSetCompressedProfileImgURL = mediaService.compressImage(toSetOriginalProfileImgURL);
            }

        }
        else{

            toSetOriginalProfileImgURL = currentOriginalProfileImgUrl;
            toSetCompressedProfileImgURL = currentCompressedProfileImgURL;

        }

        // 6. update the user information
        user.setRequestFields(
                toSetOriginalProfileImgURL,
                toSetCompressedProfileImgURL,
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
        return new UserBriefInformationResponseDto(user.getCompressedProfileImgURL(), user.getNickname(), user.getId(), user.getBalance());
    }

    private SignUpRequestDto transformUserInformationToObject(String signUpRequest) {

        ObjectMapper mapper = new ObjectMapper();
        SignUpRequestDto signUpRequestDto;

        //String ?????? ???????????? ????????? ??????
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

        //String ?????? ???????????? ????????? ??????
        try {
            updateUserRequestDto = mapper.readerFor(UpdateUserRequestDto.class).readValue(updateUserRequest);
        } catch(Exception e) {
            throw new CustomInternalErrorException(e.getMessage());
        }

        updateUserRequestDto.setUsername(username);

        return updateUserRequestDto;
    }

    public void validateUserInformation(SignUpRequestDto signUpRequestDto) {

        //null ??????
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<SignUpRequestDto>> violations = validator.validate(signUpRequestDto);

        for (ConstraintViolation<SignUpRequestDto> violation : violations) {
            throw new MissingFormatArgumentException(String.valueOf(violation.getPropertyPath()));
        }

        validatePhoneNumber(signUpRequestDto.getPhoneNumber(), null);
        validateEmail(signUpRequestDto.getEmail(), null);
        validateNickname(signUpRequestDto.getNickname(), null);
        validateRole(signUpRequestDto.getRole());

    }

    public void validateUserInformation(UpdateUserRequestDto updateUserRequestDto, User preUserInformation) {

        //null ??????
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

        validatePhoneNumber(updateUserRequestDto.getPhoneNumber(), preUserInformation.getUsername());
        validateEmail(updateUserRequestDto.getEmail(), preUserInformation.getUsername());
        validateNickname(updateUserRequestDto.getNickname(), preUserInformation.getUsername());
        validateRole(updateUserRequestDto.getRole());

    }

    public void validatePhoneNumber(String phoneNumber, String username) {

        if(username != null) {
            String currentUserPhoneNumber = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new).getPhoneNumber();
            if(currentUserPhoneNumber.equals(phoneNumber)) {
                return;
            }
        }

        if (!Pattern.matches("^(\\d{11}|\\d{3}\\d{4}\\d{4})$", phoneNumber)) {
            throw new InvalidUserInformationFormatException(FailableUserInformation.PHONENUMBER);
        }

        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        if (user.isPresent()) {
            throw new DuplicatedUserInformationException(FailableUserInformation.PHONENUMBER);
        }
    }

    public void validateEmail(String email, String username) {

        if(username != null) {
            String currentUserEmail = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new).getEmail();
            if(currentUserEmail.equals(email)) {
                return;
            }
        }

        if (!Pattern.matches("^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$", email)) {
            throw new InvalidUserInformationFormatException(FailableUserInformation.EMAIL);
        }

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            throw new DuplicatedUserInformationException(FailableUserInformation.EMAIL);
        }
    }

    public void validateNickname(String nickname, String username) {

        if(username != null) {
            String currentUserNickname = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new).getNickname();
            if(currentUserNickname.equals(nickname)) {
                return;
            }
        }

        if (!Pattern.matches("^(?=.*[a-zA-Z0-9???-???])[a-zA-Z0-9???-???]{2,16}$", nickname)) {
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
                .profileImgURL(user.getCompressedProfileImgURL())
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
