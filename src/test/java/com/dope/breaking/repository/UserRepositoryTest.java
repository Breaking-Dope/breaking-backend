package com.dope.breaking.repository;

import com.dope.breaking.domain.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    @Autowired UserRepository userRepository;

    User user = new User();

    //user.signUp

    //"URL","nickname","phoneNumber","mwk300@nyu.edu","Minwu","Kim","msg");

    @Test
    void findByNickname() {



    }

    @Test
    void findByPhoneNumber() {
    }

    @Test
    void findByEmail() {
    }

}