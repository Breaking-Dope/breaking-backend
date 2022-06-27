package com.dope.breaking.api;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class SignInAPI {

    @Autowired
    public UserRepository userRepository;

    @PostMapping("/oauth2/sign-up/exist-phonenumber")
    public Boolean validateDuplicatePhoneNumber(@RequestBody PhoneNumberRequest phoneNumberRequest){

        String phoneNumber = phoneNumberRequest.getPhoneNumber();
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        return user.isPresent();

    }

    @PostMapping("/oauth2/sign-up/exist-nickname")
    public Boolean validateDuplicateNickname(@RequestBody NicknameRequest nicknameRequest){

        String nickname = nicknameRequest.getNickname();
        Optional<User> user = userRepository.findByNickname(nickname);
        return user.isPresent();

    }

    @PostMapping("/oauth2/sign-up")
    public ResponseDto signInConfirm(@RequestBody RequestDto requestDto){

        User user = new User();
        user.SignUp(
                requestDto.getProfileImgURL(),
                requestDto.getNickname(),
                requestDto.getPhoneNumber(),
                requestDto.getEmail(),
                requestDto.getFirstName(),
                requestDto.getLastName(),
                requestDto.getStatusMsg()
        );

        userRepository.save(user);

        return new ResponseDto();

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestDto{

        private String profileImgURL;
        private String nickname;
        private String phoneNumber;
        private String email;
        private String firstName;
        private String lastName;
        private String statusMsg;

    }

    @Data
    @NoArgsConstructor
    public static class ResponseDto{

        private String status = "Created";
        private String redirectURL = "/sign-in";

    }

    @Data
    @NoArgsConstructor
    public static class PhoneNumberRequest{

        private String phoneNumber;

    }

    @Data
    @NoArgsConstructor
    public static class NicknameRequest{

        private String nickname;

    }

}
