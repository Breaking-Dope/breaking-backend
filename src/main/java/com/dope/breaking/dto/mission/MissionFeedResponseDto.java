package com.dope.breaking.dto.mission;

import com.dope.breaking.domain.post.Location;
import com.dope.breaking.dto.post.LocationDto;
import com.dope.breaking.dto.post.WriterDto;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Setter
@NoArgsConstructor
public class MissionFeedResponseDto {

    private Long missionId;

    private String title;

    private Long viewCount;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime createdDate;

    private LocationDto location;

    private WriterDto user;

    private Boolean isMyMission;

    private int postCount;

    @QueryProjection

    public MissionFeedResponseDto(Long missionId, String title, Long viewCount, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime createdDate, LocationDto location, WriterDto user, Boolean isMyMission, int postCount) {
        this.missionId = missionId;
        this.title = title;
        this.viewCount = viewCount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdDate = createdDate;
        this.location = location;
        this.user = user;
        this.isMyMission = isMyMission;
        this.postCount = postCount;
    }
}
