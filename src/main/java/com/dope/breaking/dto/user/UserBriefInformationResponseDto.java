package com.dope.breaking.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserBriefInformationResponseDto {

    String profileImgURL;

    String nickname;

    Long userId;

    int balance;



}
