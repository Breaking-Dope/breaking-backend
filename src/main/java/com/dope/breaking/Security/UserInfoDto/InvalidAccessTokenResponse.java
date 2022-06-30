package com.dope.breaking.Security.UserInfoDto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class InvalidAccessTokenResponse {

    private String AccessToken;

    private String idToken; //구글 로그인 시 필요함.

    private String ErrorMessage;
}
