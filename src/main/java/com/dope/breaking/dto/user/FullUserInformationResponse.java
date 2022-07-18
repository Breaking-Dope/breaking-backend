package com.dope.breaking.dto.user;

import com.dope.breaking.domain.user.Role;
import lombok.Builder;
import lombok.Data;

@Data
public class FullUserInformationResponse {

    String nickname;

    String phoneNumber;

    String email;

    String realName;

    Role role;

    String statusMsg;

	String profileImgURL;

    @Builder
    public FullUserInformationResponse(String nickname, String phoneNumber, String email, String realName, Role role, String statusMsg, String profileImgURL) {
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.realName = realName;
        this.role = role;
        this.statusMsg = statusMsg;
        this.profileImgURL = profileImgURL;
    }
}
