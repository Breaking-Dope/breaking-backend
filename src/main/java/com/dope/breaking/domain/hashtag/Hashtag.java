package com.dope.breaking.domain.hashtag;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
public class Hashtag {

    @Id @GeneratedValue
    @Column (name = "HASHTAG_ID")
    private Long id;

    private String hashtag;

}
