package com.dope.breaking.dto.post;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class DetailPostResponseDto {

	@JsonProperty(value = "isLiked")
    private boolean isLiked;

	@JsonProperty(value = "isBookmarked")
	private boolean isBookmarked;

	@JsonProperty(value = "isPurchased")
	private boolean isPurchased;

	private WriterDto user;

	private String title;

	private String content;

	private List<String> hashtagList = new LinkedList<>();

	private List<String> mediaList = new LinkedList<>();

	private LocationDto location;

	private int price;

	private String postType;//exclusive, public, free

	@JsonProperty(value = "isAnonymous")
	private boolean isAnonymous;

	private LocalDateTime eventDate;

	private LocalDateTime createdDate;

	private LocalDateTime modifiedDate;

	private int viewCount;

	private int shareCount;

	@JsonProperty(value = "isSold")
	private boolean isSold;

	private int soldCount;


	private int bookmarkedCount;

	@JsonProperty(value = "isHidden")
	private boolean isHidden;

	private int likeCount;

	private int commentCount;

	private Boolean isMyPost = false;

	private Boolean isPurchasable = true;

	@Builder
	public DetailPostResponseDto(boolean isLiked, boolean isBookmarked,boolean isPurchased ,WriterDto user, String title, String content, List<String> hashtagList, List<String> mediaList, LocationDto location, int price, String postType, boolean isAnonymous, LocalDateTime eventDate, LocalDateTime createdDate, LocalDateTime modifiedDate, int viewCount, boolean isSold, int soldCount, int bookmarkedCount, boolean isHidden, int likeCount, int commentCount, Boolean isMyPost, Boolean isPurchasable) {
		this.isLiked = isLiked;
		this.isBookmarked = isBookmarked;
		this.isPurchased = isPurchased;
		this.user = user;
		this.title = title;
		this.content = content;
		this.hashtagList = hashtagList;
		this.mediaList = mediaList;
		this.location = location;
		this.price = price;
		this.postType = postType;
		this.isAnonymous = isAnonymous;
		this.eventDate = eventDate;
		this.createdDate = createdDate;
		this.modifiedDate = modifiedDate;
		this.viewCount = viewCount;
		this.isSold = isSold;
		this.soldCount = soldCount;
		this.bookmarkedCount = bookmarkedCount;
		this.isHidden = isHidden;
		this.likeCount = likeCount;
		this.commentCount = commentCount;
		this.isMyPost = isMyPost;
		this.isPurchasable = isPurchasable;
	}
}

