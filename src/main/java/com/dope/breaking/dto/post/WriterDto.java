package com.dope.breaking.dto.post;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class WriterDto {
    private Long userId;

    private String profileImgURL;

    private String nickname;

    private String phoneNumber;

    @Builder
    @QueryProjection
    public WriterDto(Long userId, String profileImgURL, String nickname, String phoneNumber){
        this.userId = userId;
        this.profileImgURL = profileImgURL;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
    }
}
