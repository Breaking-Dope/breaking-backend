package com.dope.breaking.dto.post;

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
    private String address;
    @NotNull
    private Double longitude;
    @NotNull
    private Double latitude;
    @NotNull
    private String region_1depth_name;
    @NotNull
    private String region_2depth_name;

    @Builder
    public LocationDto(String address, Double longitude, Double latitude, String region_1depth_name, String region_2depth_name){
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.region_1depth_name = region_1depth_name;
        this.region_2depth_name = region_2depth_name;
    }
}
