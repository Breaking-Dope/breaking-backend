package com.dope.breaking.dto.user;

import com.dope.breaking.domain.user.Role;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Data
@Setter
public class ProfileInformationResponseDto {

    Long userId;

    @NotNull
    String profileImgURL;

    String nickname;

    String email;

    String statusMsg;

    Role role;

    int followerCount;

    int followingCount;

    boolean isFollowing;

    @Builder
    public ProfileInformationResponseDto(Long userId, String profileImgURL, String nickname, String email, String statusMsg, Role role, int followerCount, int followingCount, boolean isFollowing) {
        this.userId = userId;
        this.profileImgURL = profileImgURL;
        this.nickname = nickname;
        this.email = email;
        this.statusMsg = statusMsg;
        this.role = role;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.isFollowing = isFollowing;
    }

}
