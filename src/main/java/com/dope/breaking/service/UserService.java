package com.dope.breaking.service;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.PostResType;
import com.dope.breaking.dto.user.SignUpErrorType;
import com.dope.breaking.dto.user.SignUpRequestDto;
import com.dope.breaking.dto.user.UpdateRequestDto;
import com.dope.breaking.dto.user.UserBriefInformationResponseDto;
import com.dope.breaking.exception.auth.DuplicatedInformationException;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.auth.invalidUserInformationFormatException;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;



    private boolean isValidNicknameFormat(String email) {
    }

    public static boolean isValidRole(String role) {

        String upperCasedRole = role.toUpperCase();

        return upperCasedRole.equals("PRESS") || upperCasedRole.equals("USER");

    }

    public String invalidMessage (SignUpRequestDto signUpRequest){

        if(!isValidRole(signUpRequest.getRole())){
            return SignUpErrorType.INVALID_ROLE.getMessage();
        }

        if(!isValidEmailFormat(signUpRequest.getEmail())){
            return SignUpErrorType.INVALID_EMAIL.getMessage();
        }

        if(!isValidPhoneNumberFormat(signUpRequest.getPhoneNumber())){
            return SignUpErrorType.INVALID_PHONE_NUMBER.getMessage();
        }

        if (findByPhoneNumber(signUpRequest.getPhoneNumber()).isPresent()){
            return SignUpErrorType.PHONE_NUMBER_DUPLICATE.getMessage();
        }

        if (findByEmail(signUpRequest.getEmail()).isPresent()) {
            return SignUpErrorType.EMAIL_DUPLICATE.getMessage();
        }

        if (findByNickname(signUpRequest.getNickname()).isPresent()){
            return SignUpErrorType.NICKNAME_DUPLICATE.getMessage();
        }

        return "";

    }

    public String invalidMessage (UpdateRequestDto updateRequest, User user){

        if(!isValidRole(updateRequest.getRole())){
            return SignUpErrorType.INVALID_ROLE.getMessage();
        }

        if(!isValidEmailFormat(updateRequest.getEmail())){
            return SignUpErrorType.INVALID_EMAIL.getMessage();
        }

        if(!isValidPhoneNumberFormat(updateRequest.getPhoneNumber()) ){
            return SignUpErrorType.INVALID_PHONE_NUMBER.getMessage();
        }

        if (findByPhoneNumber(updateRequest.getPhoneNumber()).isPresent()
                && !Objects.equals(user.getPhoneNumber(), updateRequest.getPhoneNumber())) {
            return SignUpErrorType.PHONE_NUMBER_DUPLICATE.getMessage();
        }

        if (findByEmail(updateRequest.getEmail()).isPresent()
                && !Objects.equals(user.getEmail(), updateRequest.getEmail())) {
            return SignUpErrorType.EMAIL_DUPLICATE.getMessage();
        }

        if (findByNickname(updateRequest.getNickname()).isPresent()
                && !Objects.equals(user.getNickname(), updateRequest.getNickname())){
            return SignUpErrorType.NICKNAME_DUPLICATE.getMessage();
        }

        return "";

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

    public UserBriefInformationResponseDto userBriefInformation(String username) {

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        return new UserBriefInformationResponseDto(user.getProfileImgURL(), user.getNickname(), user.getId());
    }

    public void validatePhoneNumber(String phoneNumber) {

        if(Pattern.matches("^(01\\d{1}|02|0\\d{2})-?(\\d{8})", phoneNumber)){
            throw new invalidUserInformationFormatException(DuplicableUserInformation.PHONENUMBER);
        }

        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        if (user.isPresent()){
            throw new DuplicatedInformationException(DuplicableUserInformation.PHONENUMBER);
        }
    }

    public void validateEmail(String email) {

        if(Pattern.matches("^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$", email)){
            throw new invalidUserInformationFormatException(DuplicableUserInformation.EMAIL);
        }

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()){
            throw new DuplicatedInformationException(DuplicableUserInformation.EMAIL);
        }
    }

    public void validateNickname(String nickname) {

        if(Pattern.matches("^[가-힣ㄱ-ㅎa-zA-Z0-9. -]{2,}\\$", nickname)){
            throw new invalidUserInformationFormatException(DuplicableUserInformation.NICKNAME);
        }

        Optional<User> user = userRepository.findByNickname(nickname);
        if (user.isPresent()){
            throw new DuplicatedInformationException(DuplicableUserInformation.NICKNAME);
        }
    }
}
