package com.dope.breaking.dto.user;

import com.dope.breaking.domain.user.Role;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class SearchUserResponseDto {

    Long userId;

    String profileImgURL;

    String nickname;

    String email;

    String statusMsg;

    Role role;

    int followerCount;

    Boolean isFollowing;

    @Builder
    @QueryProjection
    public SearchUserResponseDto(Long userId, String profileImgURL, String nickname, String email, String statusMsg, Role role, int followerCount, Boolean isFollowing) {
        this.userId = userId;
        this.profileImgURL = profileImgURL;
        this.nickname = nickname;
        this.email = email;
        this.statusMsg = statusMsg;
        this.role = role;
        this.followerCount = followerCount;
        this.isFollowing = isFollowing;
    }
}
