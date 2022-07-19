package com.dope.breaking.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForListInfoResponseDto {

    private Long userId;
    private String nickname;
    private String statusMsg;
    private String profileImgURL;

}
