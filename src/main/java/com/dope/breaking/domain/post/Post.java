package com.dope.breaking.domain.post;

import com.dope.breaking.domain.baseTimeEntity.BaseTimeEntity;
import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.financial.Purchase;
import com.dope.breaking.domain.hashtag.Hashtag;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name="POST_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="MISSION_ID")
    private Mission mission;

    @OneToMany(mappedBy = "post")
    private List<Purchase> purchaseList = new ArrayList<Purchase>();

    @OneToMany(mappedBy="post")
    private List<Comment> commentList = new ArrayList<Comment>();

    @OneToMany(mappedBy="post", cascade = CascadeType.REMOVE)
    private List<Media> mediaList = new ArrayList<Media>();

    @OneToMany(mappedBy="post")
    private List<PostLike> postLikeList = new ArrayList<PostLike>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Hashtag> hashtags = new ArrayList<Hashtag>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Purchase> buyerList = new ArrayList<Purchase>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Bookmark> bookmarkList = new ArrayList<Bookmark>();

    private String title;

    @Column(length = 2500)
    private String content;

    @Enumerated(EnumType.STRING)
    private PostType postType;  //EXCLUSIVE, FREE, 혹은 PUBLIC

    @Embedded
    private Location location;

    private int price;

    private boolean isAnonymous;

    private boolean isSold;

    private boolean isHidden = false;

    private Boolean isPurchasable = true;

    private LocalDateTime eventDate;

    private String thumbnailImgURL;

    private int viewCount;

    @Builder
    public Post(String title, String content, PostType postType, Location location, int price, boolean isAnonymous, boolean isSold, boolean isHidden, LocalDateTime eventDate, String thumbnailImgURL, int viewCount) {
        this.title = title;
        this.content = content;
        this.postType = postType;
        this.location = location;
        this.price = price;
        this.isAnonymous = isAnonymous;
        this.isSold = isSold;
        this.isHidden = isHidden;
        this.eventDate = eventDate;
        this.thumbnailImgURL = thumbnailImgURL;
        this.viewCount = viewCount;
    }

    public void UpdatePost(String title, String content, PostType postType, Location location, int price, Boolean isAnonymous, LocalDateTime eventDate){
        this.title = title;
        this.content = content;
        this.postType = postType;
        this.location = location;
        this.price = price;
        this.isAnonymous = isAnonymous;
        this.eventDate = eventDate;
    }

    public void setUser(User user){
        this.user = user;
    }

    public void setThumbnailImgURL(String thumbnailURL){
        this.thumbnailImgURL = thumbnailURL;
    }

    public void updateViewCount() {
        this.viewCount += 1;
    }

    public void updateIsSold (boolean isSold) { this.isSold = isSold; }

    public void updateIsPurchasable (boolean isPurchasable) { this.isPurchasable = isPurchasable; }

    public void updateMission(Mission mission) { this.mission = mission; }

    public void updateIsHidden (boolean isHidden) { this.isHidden = isHidden; }

}
