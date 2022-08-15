package com.dope.breaking.repository;

import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.comment.CommentResponseDto;
import com.dope.breaking.dto.comment.SearchCommentConditionDto;

import java.util.List;

public interface CommentRepositoryCustom {

    List<CommentResponseDto> searchCommentList(User me, SearchCommentConditionDto searchCommentConditionDto, Comment cursorComment);
}
