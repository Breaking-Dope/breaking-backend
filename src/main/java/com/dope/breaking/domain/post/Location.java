package com.dope.breaking.domain.post;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Embeddable;

@Embeddable
@ToString
@NoArgsConstructor
public class Location {

    private String region;

    private Double longitude;

    private Double latitude;

    @Builder
    public Location(String region, Double longitude, Double latitude){
        this.region = region;
        this.longitude = longitude;
        this.latitude = latitude;
    }

}
