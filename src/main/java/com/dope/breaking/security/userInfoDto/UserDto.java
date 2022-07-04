package com.dope.breaking.security.userInfoDto;


import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Setter
public class UserDto {
    private String fullname;

    private String username;

    private String email;

    private String profileImgURL;
}
