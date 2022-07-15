package com.dope.breaking.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserBriefInformationResponseDto {

    String profileImgURL;

    String nickname;

    Long userId;

    int balance;

}
