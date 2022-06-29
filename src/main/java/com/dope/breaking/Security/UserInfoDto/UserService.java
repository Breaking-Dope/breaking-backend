package com.dope.breaking.Security.UserInfoDto;

import com.dope.breaking.Security.UserInfoDto.UserRepository;
import com.dope.breaking.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public Optional<User> findbyid(Long id) { return userRepository.findById(id); }

    @Transactional
    public Long Save(User user){
        return userRepository.save(user).getId();
    }


    public Optional<User> findbyUsername(String username) {
        return userRepository.findByUsername(username);
    }

}
