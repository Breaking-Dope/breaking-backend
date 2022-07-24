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
	private boolean hasLiked; //현재 접속한 유저는 좋아요를 눌렀는가에 대한 판별

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

	private LocalDateTime eventTime;

	private LocalDateTime createdDate;

	private LocalDateTime modifiedDate;

	private int viewCount;

	private int shareCount;

	@JsonProperty(value = "isSold")
	private boolean isSold;

	private int soldCount;

	@JsonProperty(value = "isHidden")
	private boolean isHidden;

	@Builder
	public DetailPostResponseDto(boolean hasLiked, WriterDto writerDto, String title, String content, List<String> hashtagList, List<String> mediaList, LocationDto location, int price, String postType, boolean isAnonymous, LocalDateTime eventTime, LocalDateTime createdDate, LocalDateTime modifiedDate, int viewCount, int shareCount, boolean isSold, int soldCount, boolean isHidden){
		this.hasLiked = hasLiked;
		this.user = writerDto;
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

