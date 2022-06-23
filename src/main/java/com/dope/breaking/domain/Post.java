package com.dope.breaking.domain;



import com.dope.breaking.domain.Financials.Purchase;
import com.dope.breaking.domain.Like.PostLike;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Post {

    @Id @GeneratedValue
    @Column (name="POST_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="USER_ID")
    private User user;

    @OneToMany(mappedBy = "post")
    private List<Comment> commentList = new ArrayList<Comment>();

    @OneToMany(mappedBy="post")
    private List<PostLike> postLikeList = new ArrayList<PostLike>();

    @OneToMany(mappedBy="post")
    private List<Media> mediaList = new ArrayList<Media>();

    @OneToMany(mappedBy = "post")
    private List<Purchase> buyerList = new ArrayList<Purchase>();

    private String content;

    @Enumerated(EnumType.STRING)
    private PostType postType;  //EXCLUSIVE, FREE, 혹은 PUBLIC

    @Embedded
    private Location location;

    private int price;

    private boolean isAnonymous;
    private boolean isSold;
    private boolean isHidden;

    //나중에 테스트 후 conversion 주의
    private LocalDateTime eventTime;
}
