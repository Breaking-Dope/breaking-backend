package com.dope.breaking.domain.media;

import com.dope.breaking.domain.post.Post;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class Media {

    @Id @GeneratedValue
    @Column(name="MEDIA_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "POST_ID")
    private Post post;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;   // PHOTO 혹은 VIDEO

    private String mediaURL;
}
