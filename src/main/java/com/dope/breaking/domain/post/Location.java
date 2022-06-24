package com.dope.breaking.domain.post;

import javax.persistence.Embeddable;

@Embeddable
public class Location {

    private String region;

    private Double longitude;

    private Double latitude;

}
