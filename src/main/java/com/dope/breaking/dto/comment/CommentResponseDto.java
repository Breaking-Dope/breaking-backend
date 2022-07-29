package com.dope.breaking.dto.comment;

import com.dope.breaking.dto.post.WriterDto;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Data
public class CommentResponseDto {

    private Long commentId;

    private String content;

    private Long likeCount;

    private Long replyCount;

    private WriterDto user;

    private Boolean isLiked;

    private LocalDateTime createdTime;

}
