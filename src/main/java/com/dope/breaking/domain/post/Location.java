package com.dope.breaking.domain.post;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Embeddable;

@Embeddable
@ToString
@Getter
@NoArgsConstructor
public class Location {

    private String address;

    private Double longitude;

    private Double latitude;

    private String region_1depth_name;

    private String region_2depth_name;

    @Builder
    public Location(String address, Double longitude, Double latitude, String region_1depth_name, String region_2depth_name){
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.region_1depth_name = region_1depth_name;
        this.region_2depth_name = region_2depth_name;
    }

}
