package com.dope.breaking.Security.UserInfoDto;


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
