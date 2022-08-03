package com.dope.breaking.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForListInfoResponseDto {

    private Long userId;
    private String nickname;
    private String statusMsg;
    private String profileImgURL;
    @JsonProperty(value = "isFollowing")
    private boolean isFollowing = false;

}
