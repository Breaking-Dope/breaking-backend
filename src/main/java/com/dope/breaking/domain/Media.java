package com.dope.breaking.domain;

import javax.persistence.*;

@Entity
public class Media {
    @Id
    @GeneratedValue
    @Column(name="MEDIA_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "POST_ID")
    private Post post;

    private String mediaURL;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;   // PHOTO 혹은 VIDEO
}
