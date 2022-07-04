package com.dope.breaking.security.userInfoDto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidAccessTokenResponse {

    private String AccessToken;

    private String idToken; //구글 로그인 시 필요함.

    private String ErrorMessage;
}
