package com.dope.breaking.dto.post;

import com.dope.breaking.domain.post.Post;
import lombok.Data;

import java.util.List;

@Data
public class SearchFeedResponseDto {
    private List<Post> postList;
    private int currentPage;
}
