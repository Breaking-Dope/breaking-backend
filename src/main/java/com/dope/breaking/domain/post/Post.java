package com.dope.breaking.domain.post;



import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.financial.Purchase;
import com.dope.breaking.domain.media.Media;
import com.dope.breaking.domain.user.User;
import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
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

}
