package com.dope.breaking.security.controller;

public class GoogleDto {
    String accessToken;
    String idToken;

    GoogleDto(String accessToken, String idToken){
        this.accessToken = accessToken;
        this.idToken = idToken;
    }

    @Override
    public String toString() {
        return "accesstoken : " + accessToken + ", idToken : " + idToken;
    }
}
