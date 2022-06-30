package com.dope.breaking.service;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
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

    public Optional<User> findbyUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
