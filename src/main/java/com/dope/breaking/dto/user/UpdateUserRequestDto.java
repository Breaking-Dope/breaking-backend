package com.dope.breaking.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequestDto {

    private String statusMsg;
    @NotNull
    private String nickname;
    @NotNull
    private String phoneNumber;
    @NotNull
    private String email;
    @NotNull
    private String realName;
    @NotNull
    private String role;
    private String Username;

}


