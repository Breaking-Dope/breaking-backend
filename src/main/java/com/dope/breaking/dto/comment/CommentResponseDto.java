package com.dope.breaking.dto.comment;

import com.dope.breaking.dto.post.WriterDto;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@NoArgsConstructor
@Data
public class CommentResponseDto {

    private Long commentId;

    private String content;

    private Integer likeCount;

    private Integer replyCount;

    private WriterDto user;

    private Boolean isLiked;

    private LocalDateTime createdDate;

    @QueryProjection
    public CommentResponseDto(Long commentId, String content, Integer likeCount, Integer replyCount, WriterDto user, Boolean isLiked, LocalDateTime createdDate) {
        this.commentId = commentId;
        this.content = content;
        this.likeCount = likeCount;
        this.replyCount = replyCount;
        this.user = user;
        this.isLiked = isLiked;
        this.createdDate = createdDate;
    }
}
