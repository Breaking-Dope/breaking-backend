package com.dope.breaking.dto.post;

import com.dope.breaking.domain.post.PostType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Setter
public class FeedResultPostDto {
    private Long postId;
    private String title;
    private String region;
    private String thumbnailImgURL;
    private int likeCount;
    private PostType postType;
    private Boolean isSold;
    private int viewCount;
    private Long userId;
    private String profileImgURL;
    private String realName;
    private Boolean isLiked;
    private Boolean isBookmarked;

    @QueryProjection
    public FeedResultPostDto(Long postId, String title, String region, String thumbnailImgURL, int likeCount,
                             PostType postType, Boolean isSold, int viewCount, Long userId, String profileImgURL,
                             String realName, Boolean isLiked, Boolean isBookmarked) {
        this.postId = postId;
        this.title = title;
        this.region = region;
        this.thumbnailImgURL = thumbnailImgURL;
        this.likeCount = likeCount;
        this.postType = postType;
        this.isSold = isSold;
        this.viewCount = viewCount;
        this.userId = userId;
        this.profileImgURL = profileImgURL;
        this.realName = realName;
        this.isLiked = isLiked;
        this.isBookmarked = isBookmarked;
    }

}
