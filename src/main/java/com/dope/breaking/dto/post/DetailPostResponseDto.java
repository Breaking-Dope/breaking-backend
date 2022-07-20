package com.dope.breaking.dto.post;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class DetailPostResponseDto {


    private boolean hasLiked; //현재 접속한 유저는 좋아요를 눌렀는가에 대한 판별

	private Writer user;

	private String title;
    private String content;
	private List<String> hashtagList = new LinkedList<>();

	private List<String> mediaList = new LinkedList<>();


	private LocationDto location;

	private int price;

	private String postType;//exclusive, public, free

	private boolean isAnonymous;

	private LocalDateTime eventTime;

	private LocalDateTime createdDate;

	private LocalDateTime modifiedDate;

	private int viewCount;

	private int shareCount;

	private boolean isSold;

	private int soldCount;

	private boolean isHidden;

	@Builder
	public DetailPostResponseDto(boolean hasLiked, Writer writer, String title, String content, List<String> hashtagList, List<String> mediaList, LocationDto location, int price, String postType, boolean isAnonymous, LocalDateTime eventTime, LocalDateTime createdDate, LocalDateTime modifiedDate, int viewCount, int shareCount, boolean isSold, int soldCount, boolean isHidden){
		this.hasLiked = hasLiked;
		this.user = writer;
		this.title = title;
		this.content = content;
		this.hashtagList = hashtagList;
		this.mediaList = mediaList;
		this.location = location;
		this.price = price;
		this.postType = postType;
		this.isAnonymous = isAnonymous;
		this.eventTime = eventTime;
		this.createdDate = createdDate;
		this.modifiedDate = modifiedDate;
		this.viewCount = viewCount;
	}

}
