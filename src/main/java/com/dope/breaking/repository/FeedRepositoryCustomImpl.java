package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.QFeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.service.SoldOption;
import com.dope.breaking.service.SortStrategy;
import com.dope.breaking.service.UserPageFeedOption;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.dope.breaking.domain.post.QPostLike.*;
import static com.dope.breaking.domain.post.QPost.post;
import static com.dope.breaking.domain.user.QUser.user;

@Repository
public class FeedRepositoryCustomImpl implements FeedRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final BookmarkRepository bookmarkRepository;

    private final PostLikeRepository postLikeRepository;

    public FeedRepositoryCustomImpl(EntityManager em, BookmarkRepository bookmarkRepository, PostLikeRepository postLikeRepository) {
        this.queryFactory = new JPAQueryFactory(em);
        this.bookmarkRepository = bookmarkRepository;
        this.postLikeRepository = postLikeRepository;
    }

    @Override
    public List<FeedResultPostDto> searchFeedBy(SearchFeedConditionDto searchFeedConditionDto, Post cursorPost, User me) {

        List<Tuple> paginatedResult = queryFactory
                .select(post.id, postLike.post.id.count())
                .from(post)
                .leftJoin(post.postLikeList, postLike)
                .where(
                        post.isHidden.eq(false),
                        userPageFeedOption(searchFeedConditionDto.getUserPageFeedOption(), searchFeedConditionDto.getOwnerId()),
                        soldOption(searchFeedConditionDto.getSoldOption()),
                        period(searchFeedConditionDto.getDateFrom(), searchFeedConditionDto.getDateTo()),
                        cursorPagination(cursorPost, searchFeedConditionDto.getSortStrategy()),
                        sameLevelCursorFilter(cursorPost, searchFeedConditionDto.getSortStrategy())
                )
                .groupBy(post.id, postLike.post.id)
                .orderBy(boardSort(searchFeedConditionDto.getSortStrategy()), boardSort(SortStrategy.CHRONOLOGICAL))
                .limit(searchFeedConditionDto.getSize())
                .fetch();

        List<Long> contentIdList = paginatedResult.stream().map(x -> x.get(post.id)).collect(Collectors.toList());

        List<FeedResultPostDto> content = queryFactory
                .select(new QFeedResultPostDto(
                        post.id,
                        post.title,
                        post.location.region,
                        post.thumbnailImgURL,
                        post.postLikeList.size(),
                        post.postType,
                        post.isSold,
                        post.viewCount,
                        user.id,
                        user.compressedProfileImgURL,
                        user.nickname,
                        post.price,
                        Expressions.asBoolean(false),
                        Expressions.asBoolean(false),
                        post.createdDate
                ))
                .from(post)
                .leftJoin(post.user, user)
                .where(post.id.in(contentIdList))
                .orderBy(boardSort(searchFeedConditionDto.getSortStrategy()), boardSort(SortStrategy.CHRONOLOGICAL))
                .fetch();

        for(FeedResultPostDto dto : content) {
            dto.setIsBookmarked(bookmarkRepository.existsByUserAndPostId(me, dto.getPostId()));
            dto.setIsLiked(postLikeRepository.existsByUserAndPostId(me, dto.getPostId()));
        }

        return content;
    }

    private Predicate userPageFeedOption(UserPageFeedOption userPageFeedOption, Long ownerId) {

        if(userPageFeedOption == null) {
            return null;
        }

        switch (userPageFeedOption) {
            case BUY:
            case BOOKMARK:
            case WRITE:
            default:
                return post.user.id.eq(ownerId);
        }
    }

    private Predicate soldOption(SoldOption soldOption) {

        switch (soldOption) {
            case SOLD:
                return post.isSold.eq(true);
            case UNSOLD:
                return post.isSold.eq(false);
            case ALL:
            default:
                return null;
        }

    }

    private Predicate period(LocalDateTime dateFrom, LocalDateTime dateTo) {
        if(dateFrom == null || dateTo == null) {
            return null;
        } else {
            return post.createdDate.between(dateFrom, dateTo);
        }
    }

    private Predicate cursorPagination(Post cursorPost, SortStrategy sortStrategy) {

        if(cursorPost == null || sortStrategy == null) {
            return null;
        }

        switch (sortStrategy) {
            case LIKE:
                return post.postLikeList.size().loe(cursorPost.getPostLikeList().size());
            case VIEW:
                return post.viewCount.loe(cursorPost.getViewCount());
            case CHRONOLOGICAL:
            default:
                return post.id.lt(cursorPost.getId());
        }
    }

    private Predicate sameLevelCursorFilter(Post cursorPost, SortStrategy sortStrategy) {

        if(cursorPost == null || sortStrategy == null) {
            return null;
        }

        switch (sortStrategy) {
            case LIKE:
                return ExpressionUtils.or(post.postLikeList.size().ne(cursorPost.getPostLikeList().size()), post.id.lt(cursorPost.getId()));
            case VIEW:
                return ExpressionUtils.or(post.viewCount.ne(cursorPost.getViewCount()), post.id.lt(cursorPost.getId()));
            case CHRONOLOGICAL:
            default:
                return null;
        }
    }

    private OrderSpecifier<?> boardSort(SortStrategy sortStrategy) {

        if(sortStrategy == null){
            return new OrderSpecifier<>(Order.DESC, post.id);
        }

        switch (sortStrategy){
            case LIKE:
                return new OrderSpecifier<>(Order.DESC, post.postLikeList.size());
            case VIEW:
                return new OrderSpecifier<>(Order.DESC, post.viewCount);
            case CHRONOLOGICAL:
                return new OrderSpecifier<>(Order.DESC, post.id);
        }

        return new OrderSpecifier<>(Order.DESC, post.id);

    }

}
