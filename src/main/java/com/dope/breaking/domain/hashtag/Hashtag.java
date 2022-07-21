package com.dope.breaking.domain.hashtag;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Hashtag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "HASHTAG_ID")
    private Long id;

    private String hashtag;


    @Builder
    public Hashtag(String hashtag){
        this.hashtag = hashtag;
    }
}
