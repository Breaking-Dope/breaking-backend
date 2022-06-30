package com.dope.breaking.service;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public static boolean isValidEmail(String email) {

        boolean err = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);

        if(m.matches()) {

            err = true;

        }
        return err;
    }

    public Optional<User> findByUsername(String username) {

        return userRepository.findByUsername(username);

    }

    public Optional<User> findByNickname(String nickname) {

        return userRepository.findByNickname(nickname);

    }

    public Optional<User> findByPhoneNumber(String phoneNumber) {

        return userRepository.findByPhoneNumber(phoneNumber);

    }

    public Optional<User> findByEmail(String email) {

        return userRepository.findByEmail(email);

    }

    public Optional<User> findById(Long id){

        return userRepository.findById(id);

    }

    public User save(User user){

        return userRepository.save(user);

    }
}
