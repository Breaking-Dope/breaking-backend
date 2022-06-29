package com.dope.breaking.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserService {

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
}
