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

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocationDto location;

    private WriterDto user;

    private Boolean isMyMission;

    @QueryProjection
    public MissionFeedResponseDto(Long missionId, String title, LocalDateTime startTime, LocalDateTime endTime, LocationDto location, WriterDto user, Boolean isMyMission) {
        this.missionId = missionId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.user = user;
        this.isMyMission = isMyMission;
    }
}