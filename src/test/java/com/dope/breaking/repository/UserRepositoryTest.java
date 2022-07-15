package com.dope.breaking.repository;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void findByNickname() {

        //Given
        User user = new User();
        user.setRequestFields("URL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);

        //When
        User savedUser = userRepository.save(user);
        User foundUser = userRepository.findByNickname(savedUser.getNickname()).get();

        //Then
        Assertions.assertThat(foundUser).isEqualTo(savedUser);

    }

//    @Test
//    void findByPhoneNumber() {
//
//        //Given
//        User user = new User();
//        user.setRequestFields("URL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username",Role.USER);
//
//        //When
//        User savedUser = userService.save(user);
//        User foundUser = userService.findByPhoneNumber(savedUser.getPhoneNumber()).get();
//
//        //Then
//        Assertions.assertThat(foundUser).isEqualTo(savedUser);
//
//    }

    @Test
    void findByEmail() {

        //Given
        User user = new User();
        user.setRequestFields("URL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username",Role.USER);

        //When
        User savedUser = userRepository.save(user);
        User foundUser = userRepository.findByEmail(savedUser.getEmail()).get();

        //Then
        Assertions.assertThat(foundUser).isEqualTo(savedUser);

    }

}