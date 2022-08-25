package com.dope.breaking.service;

import com.dope.breaking.domain.media.UploadType;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.*;
import com.dope.breaking.exception.CustomInternalErrorException;
import com.dope.breaking.exception.NotValidRequestBodyException;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.auth.NotFoundUserAgent;
import com.dope.breaking.exception.user.DuplicatedUserInformationException;
import com.dope.breaking.exception.user.InvalidUserInformationFormatException;
import com.dope.breaking.exception.user.NoSuchUserException;
import com.dope.breaking.repository.BookmarkRepository;
import com.dope.breaking.repository.FollowRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import com.dope.breaking.security.jwt.DistinguishUserAgent;
import com.dope.breaking.security.jwt.JwtTokenProvider;
import com.dope.breaking.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final MediaService mediaService;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;

    private final JwtTokenProvider jwtTokenProvider;

    private final DistinguishUserAgent distinguishUserAgent;

    private final RedisService redisService;


    @Transactional
    public ResponseEntity<?> signUp(String signUpRequest, List<MultipartFile> profileImg, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

        String userAgent = Optional.ofNullable(httpServletRequest.getHeader("User-Agent")).orElseThrow(() -> new NotFoundUserAgent());

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
        String userAgentType = distinguishUserAgent.extractUserAgent(userAgent);
        httpHeaders.set("authorization", jwtTokenProvider.createAccessToken(signUpRequestDto.getUsername(), userAgentType));
        String refreshjwt = jwtTokenProvider.createRefreshToken(signUpRequestDto.getUsername());
        if(userAgentType.equals("WEB")) {
            Cookie cookie = new Cookie("authorization-refresh", refreshjwt);
            cookie.setMaxAge(14 * 24 * 60 * 60); //2주
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            httpServletResponse.addCookie(cookie);
        }
        else{
            httpHeaders.set("authorization-refresh", refreshjwt);
        }
        redisService.setDataWithExpiration(userAgentType + "_" + signUpRequestDto.getUsername(), refreshjwt, 2 * 604800L); //리플리쉬 토큰 redis에 저장.

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


        if (updateUserRequestDto.getIsProfileImgChanged()) {

            // case 1: 유저 본인 선택 이미지 -> 기본 이미지
            if (currentOriginalProfileImgUrl != null && profileImg == null) {
                File file1 = new File(mediaService.getMAIN_DIR_NAME() + currentOriginalProfileImgUrl);
                File file2 = new File(mediaService.getMAIN_DIR_NAME() + currentCompressedProfileImgURL);
                file1.delete();
                file2.delete();
            }

            // case 2: 기본 이미지 -> 유저 본인 선택 이미지
            else if (currentOriginalProfileImgUrl == null && profileImg != null) {
                toSetOriginalProfileImgURL = mediaService.uploadMedias(profileImg, UploadType.ORIGNAL_PROFILE_IMG).get(0);
                toSetCompressedProfileImgURL = mediaService.compressImage(toSetOriginalProfileImgURL);
            }

            // case 3: 유저 본인 선택 이미지 -> 유저 본인 선택 이미지
            else if (currentOriginalProfileImgUrl != null && profileImg != null) {
                File file1 = new File(mediaService.getMAIN_DIR_NAME() + currentOriginalProfileImgUrl);
                File file2 = new File(mediaService.getMAIN_DIR_NAME() + currentCompressedProfileImgURL);
                file1.delete();
                file2.delete();

                toSetOriginalProfileImgURL = mediaService.uploadMedias(profileImg, UploadType.ORIGNAL_PROFILE_IMG).get(0);
                toSetCompressedProfileImgURL = mediaService.compressImage(toSetOriginalProfileImgURL);
            }

        } else {

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
        } catch (Exception e) {
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

        validatePhoneNumber(signUpRequestDto.getPhoneNumber(), null);
        validateEmail(signUpRequestDto.getEmail(), null);
        validateNickname(signUpRequestDto.getNickname(), null);
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

        validatePhoneNumber(updateUserRequestDto.getPhoneNumber(), preUserInformation.getUsername());
        validateEmail(updateUserRequestDto.getEmail(), preUserInformation.getUsername());
        validateNickname(updateUserRequestDto.getNickname(), preUserInformation.getUsername());
        validateRole(updateUserRequestDto.getRole());

    }

    public void validatePhoneNumber(String phoneNumber, String username) {

        if (username != null) {
            String currentUserPhoneNumber = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new).getPhoneNumber();
            if (currentUserPhoneNumber.equals(phoneNumber)) {
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

        if (username != null) {
            String currentUserEmail = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new).getEmail();
            if (currentUserEmail.equals(email)) {
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

        if (username != null) {
            String currentUserNickname = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new).getNickname();
            if (currentUserNickname.equals(nickname)) {
                return;
            }
        }

        if (!Pattern.matches("^(?=.*[a-zA-Z0-9가-힣])[a-zA-Z0-9가-힣]{2,16}$", nickname)) {
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


    public ProfileInformationResponseDto profileInformation(String username, Long userId) {

        User user = userRepository.findById(userId).orElseThrow(NoSuchUserException::new);
        int followerCount = followRepository.countFollowsByFollowed(user);
        int followingCount = followRepository.countFollowsByFollowing(user);
        int postCount = postRepository.countPostByUser(user);

        boolean isFollowing = false;
        if (username != null) {
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
                .postCount(postCount)
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

    @Transactional
    public void withdraw(String username) {

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        followRepository.deleteAllByFollowing(user);
        followRepository.deleteAllByFollowed(user);
        bookmarkRepository.deleteAllByUser(user);

        ArrayList<String> profileImageList = new ArrayList<>();
        profileImageList.add(user.getOriginalProfileImgURL());
        profileImageList.add(user.getCompressedProfileImgURL());

        user.removeUserInformation();

        mediaService.deleteMedias(profileImageList);
    }
}
