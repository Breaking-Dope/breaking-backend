package com.dope.breaking.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRequestDto {

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

}


