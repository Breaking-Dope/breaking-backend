package com.dope.breaking.dto.post;

import com.dope.breaking.domain.post.Location;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Setter
@Getter
@ToString
@JsonRootName(value = "location")
public class LocationDto {
    @NotNull
    private String region;
    @NotNull
    private Double longitude;
    @NotNull
    private Double latitude;

    @Builder
    public LocationDto(String region, Double longitude, Double latitude){
        this.region = region;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
