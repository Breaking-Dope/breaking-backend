package com.dope.breaking.domain;

import javax.persistence.Embeddable;

@Embeddable
public class Location {
    private String region;
    private Double longitude;
    private Double latitude;
}
