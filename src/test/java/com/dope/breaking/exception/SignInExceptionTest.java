package com.dope.breaking.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SignInExceptionTest {

    @Test
    void isValidEmail() {
        String email1 = "hello@naver.com";
        String email2 = "hello@naver";
        String email3 = "hello";

        assertTrue(SignInException.isValidEmail(email1));
        assertFalse(SignInException.isValidEmail(email2));
        assertFalse(SignInException.isValidEmail(email3));



    }
}