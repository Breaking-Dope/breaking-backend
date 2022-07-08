package com.dope.breaking.domain.post;



import com.dope.breaking.domain.baseTimeEntity.BaseTimeEntity;
import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.financial.Purchase;
import com.dope.breaking.domain.media.Media;
import com.dope.breaking.domain.user.Bookmark;
import com.dope.breaking.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ToString
@Entity
@Getter
@NoArgsConstructor
public class Post extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @OneToMany(mappedBy = "post")
    private List<Bookmark> bookmarkList = new ArrayList<Bookmark>();

    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    private PostType postType;  //EXCLUSIVE, FREE, 혹은 PUBLIC

    @Embedded
    private Location location;

    private int price;

    private boolean isAnonymous;

    private boolean isSold;

    private boolean isHidden;

    private LocalDateTime eventTime;

    private String thumbnailImgURL;

    private int viewCount;

    @Builder
    public Post(String title, String content, PostType postType, Location location, int price, boolean isAnonymous, LocalDateTime eventTime){
        this.title = title;
        this.content = content;
        this.postType = postType;
        this.location = location;
        this.price = price;
        this.isAnonymous = isAnonymous;
        this.eventTime = eventTime;
        this.isSold = false;
        this.isHidden = false;
    }

    public void UpdatePost(String title, String content, PostType postType, Location location, int price, Boolean isAnonymous, LocalDateTime eventTime){
        this.title = title;
        this.content = content;
        this.postType = postType;
        this.location = location;
        this.price = price;
        this.isAnonymous = isAnonymous;
        this.eventTime = eventTime;
    }

    public void setUser(User user){
        this.user = user;
    }

    public void setThumbnailImgURL(String thumbnailURL){
        this.thumbnailImgURL = thumbnailURL;
    }

}
