package com.dope.breaking.dto.user;

import com.dope.breaking.domain.user.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequestDto {

    private String statusMsg;
    @NotNull
    private String nickname;
    @NotNull
    private String phoneNumber;
    @NotNull
    private String email;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private String username;
    @NotNull
    private String role;

}


