package com.dope.breaking.domain.media;

import com.dope.breaking.domain.post.Post;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class Media {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="MEDIA_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "POST_ID")
    private Post post;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;   // PHOTO 혹은 VIDEO

    private String mediaURL;

    @Builder
    public Media(Post post, MediaType mediaType, String fileName){
        this.post = post;
        this.mediaType = mediaType;
        this.mediaURL = fileName;
    }

    protected Media(){}

}
