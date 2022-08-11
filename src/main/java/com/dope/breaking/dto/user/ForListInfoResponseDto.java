package com.dope.breaking.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForListInfoResponseDto {

    private Long cursorId;
    private Long userId;
    private String nickname;
    private String statusMsg;
    private String profileImgURL;
    @JsonProperty(value = "isFollowing")
    private boolean isFollowing = false;

    @QueryProjection
    public ForListInfoResponseDto(Long cursorId, Long userId, String nickname, String statusMsg, String profileImgURL) {
        this.cursorId = cursorId;
        this.userId = userId;
        this.nickname = nickname;
        this.statusMsg = statusMsg;
        this.profileImgURL = profileImgURL;
    }

}
