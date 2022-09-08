package com.dope.breaking.dto.post;

import com.dope.breaking.domain.post.PostType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Setter
public class FeedResultPostDto {
    private Long postId;
    private String title;
    private LocationDto location;
    private String thumbnailImgURL;
    private int likeCount;
    private int commentCount;
    private PostType postType;
    private int viewCount;
    private WriterDto user;
    private int price;
    private LocalDateTime createdDate;
    private Boolean isPurchasable;
    private Boolean isSold;
    private Boolean isAnonymous;
    private Boolean isHidden;
    private Boolean isMyPost;
    private Boolean isLiked;
    private Boolean isBookmarked;

    @QueryProjection
    @Builder
    public FeedResultPostDto(Long postId, String title, LocationDto location, String thumbnailImgURL, int likeCount, int commentCount, PostType postType, int viewCount, WriterDto user, int price, LocalDateTime createdDate, Boolean isPurchasable, Boolean isSold, Boolean isAnonymous, Boolean isHidden, Boolean isMyPost, Boolean isLiked, Boolean isBookmarked) {
        this.postId = postId;
        this.title = title;
        this.location = location;
        this.thumbnailImgURL = thumbnailImgURL;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.postType = postType;
        this.viewCount = viewCount;
        this.user = user;
        this.price = price;
        this.createdDate = createdDate;
        this.isPurchasable = isPurchasable;
        this.isSold = isSold;
        this.isAnonymous = isAnonymous;
        this.isHidden = isHidden;
        this.isMyPost = isMyPost;
        this.isLiked = isLiked;
        this.isBookmarked = isBookmarked;
    }
}
