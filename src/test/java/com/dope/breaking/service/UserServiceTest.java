package com.dope.breaking.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void isValidEmail() {

        String email1 = "hello@naver.com";
        String email2 = "hello@naver";
        String email3 = "hello";

        assertTrue(UserService.isValidEmail(email1));
        assertFalse(UserService.isValidEmail(email2));
        assertFalse(UserService.isValidEmail(email3));

    }

}