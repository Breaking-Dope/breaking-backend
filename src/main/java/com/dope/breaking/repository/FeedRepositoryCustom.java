package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;

import java.util.List;

public interface FeedRepositoryCustom {

    List<FeedResultPostDto> searchFeedBy(SearchFeedConditionDto searchFeedConditionDto, Post cursorPost, User me);

    List<FeedResultPostDto> searchUserPageBy(SearchFeedConditionDto searchFeedConditionDto, User owner, User me, Post cursorPost);

}
