package com.dope.breaking.dto.user;


import com.dope.breaking.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserBasicInformationResponseDto {
    Long userId;

    String profileImgURL;

    String nickname;

    @Builder
    public UserBasicInformationResponseDto(Long userId, String profileImgURL, String nickname){
        this.userId = userId;
        this.profileImgURL = profileImgURL;
        this.nickname = nickname;
    }
}
