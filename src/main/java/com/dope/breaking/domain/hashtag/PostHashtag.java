package com.dope.breaking.domain.hashtag;


import com.dope.breaking.domain.post.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
public class PostHashtag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name =  "POST_HASHTAG_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "POST_ID")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "HASHTAG_ID")
    private Hashtag hashtag;

    @Builder
    public PostHashtag(Post post, Hashtag hashtag){
        this.post = post;
        this.hashtag = hashtag;
    }
}
