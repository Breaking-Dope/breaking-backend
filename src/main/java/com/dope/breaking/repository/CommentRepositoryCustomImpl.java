package com.dope.breaking.repository;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.comment.CommentResponseDto;
import com.dope.breaking.dto.comment.QCommentResponseDto;
import com.dope.breaking.dto.comment.SearchCommentConditionDto;
import com.dope.breaking.dto.post.QWriterDto;
import com.dope.breaking.service.CommentTargetType;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.dope.breaking.domain.comment.QComment.comment;
import static com.dope.breaking.domain.user.QUser.user;

@Repository
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private final CommentLikeRepository commentLikeRepository;

    public CommentRepositoryCustomImpl(EntityManager em, CommentLikeRepository commentLikeRepository) {
        this.queryFactory = new JPAQueryFactory(em);
        this.commentLikeRepository = commentLikeRepository;
    }

    @Override
    public List<CommentResponseDto> searchCommentList(User me, SearchCommentConditionDto searchCommentConditionDto) {

        List<CommentResponseDto> content = queryFactory
                .select(new QCommentResponseDto(
                            comment.id,
                            comment.content,
                            comment.commentLikeList.size(),
                            comment.children.size(),
                            new QWriterDto(
                                user.id,
                                user.compressedProfileImgURL,
                                user.nickname,
                                Expressions.asString("")
                            ),
                            Expressions.asBoolean(false),
                            comment.createdTime
                        ))
                .from(comment)
                .leftJoin(comment.user, user)
                .where(
                        target(searchCommentConditionDto.getTargetType(), searchCommentConditionDto.getTargetId()),
                        cursorPagination(searchCommentConditionDto.getCursorId())
                )
                .limit(searchCommentConditionDto.getSize())
                .fetch();


        if(me != null) {
            for(CommentResponseDto commentResponseDto : content) {
                if(commentLikeRepository.existsCommentLikeByUserAndCommentId(me, commentResponseDto.getCommentId())) {
                    commentResponseDto.setIsLiked(true);
                }
            }
        }

        return content;
    }

    private Predicate cursorPagination(Long cursorId) {
        if(cursorId == null || cursorId == 0) {
            return null;
        }
        return comment.id.gt(cursorId);
    }

    private Predicate target(CommentTargetType targetType, Long targetId) {

        if(targetId == null || targetId == 0) {
            return null;
        }

        switch (targetType) {
            case POST:
                return comment.post.id.eq(targetId);
        }
        return null;
    }
}
