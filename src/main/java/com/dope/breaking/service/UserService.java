package com.dope.breaking.service;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.PostResType;
import com.dope.breaking.dto.response.MessageResponseDto;
import com.dope.breaking.dto.user.SignUpErrorType;
import com.dope.breaking.dto.user.SignUpRequestDto;
import com.dope.breaking.dto.user.UpdateRequestDto;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public static boolean isValidEmailFormat(String email) {

        boolean err = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);

        if(m.matches()) {
            err = true;
        }
        return err;

    }

    public static boolean isValidPhoneNumberFormat(String phoneNumber){

        boolean err = false;
        String regex = "^(01\\d{1}|02|0\\d{2})-?(\\d{8})";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(phoneNumber);

        if (m.matches()){
            err = true;
        }
        return err;

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

    public String validateUsername(Principal principal){

        if (principal == null){
            return PostResType.NOT_FOUND_USER.getMessage();
        }
        if (!existByUsername(principal.getName())){
            return PostResType.NOT_REGISTERED_USER.getMessage();
        }
        return "";
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    public Optional<User> findByPhoneNumber(String phoneNumber) { return userRepository.findByPhoneNumber(phoneNumber); }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id){
        return userRepository.findById(id);
    }

    public User save(User user) { return userRepository.save(user); }

    public Boolean existByUsername(String username){
        return userRepository.existsByUsername(username);
    }

    public Boolean existById(Long userId){
        return userRepository.existsById(userId);
    }

    public void deleteUser(User user){userRepository.delete(user);}

}
