package com.dope.breaking.dto.comment;

import com.dope.breaking.service.CommentTargetType;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class SearchCommentConditionDto {

    public CommentTargetType targetType;

    private Long targetId;

    private Long cursorId;

    private Long size;

    @Builder
    public SearchCommentConditionDto(CommentTargetType targetType, Long targetId, Long cursorId, Long size) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.cursorId = cursorId;
        this.size = size;
    }
}
