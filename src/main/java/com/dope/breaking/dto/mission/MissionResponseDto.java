package com.dope.breaking.dto.mission;

import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.dto.post.LocationDto;
import com.dope.breaking.dto.post.WriterDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class MissionResponseDto {

    @JsonProperty(value = "isMyMission")
    Boolean isMyMission;

    String title;

    String content;

    LocalDateTime startTime;

    LocalDateTime endTime;


    LocationDto locationDto;

    WriterDto user;

    @Builder
    public MissionResponseDto(String title, String content, LocalDateTime startTime, LocalDateTime endTime, Boolean isMyMission, LocationDto locationDto, WriterDto writerDto){
        this.isMyMission = isMyMission;
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.locationDto = locationDto;
        this.user = writerDto;
    }

}
