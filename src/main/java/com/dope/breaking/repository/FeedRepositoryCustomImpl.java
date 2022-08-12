package com.dope.breaking.repository;

import com.dope.breaking.domain.hashtag.QHashtag;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.*;
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
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.dope.breaking.domain.financial.QPurchase.purchase;
import static com.dope.breaking.domain.hashtag.QHashtag.*;
import static com.dope.breaking.domain.post.QPostLike.*;
import static com.dope.breaking.domain.post.QPost.post;
import static com.dope.breaking.domain.user.QBookmark.bookmark;
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
                .leftJoin(post.hashtags, hashtag)
                .where(
                        post.isHidden.eq(false),
                        keyWordSearch(searchFeedConditionDto.getSearchKeyword()),
                        hashtagSearch(searchFeedConditionDto.getSearchHashtag()),
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
                        new QLocationDto(
                                post.location.address,
                                post.location.longitude,
                                post.location.latitude,
                                post.location.region_1depth_name,
                                post.location.region_2depth_name
                        ),
                        post.thumbnailImgURL,
                        post.postLikeList.size(),
                        Expressions.asNumber(0),
                        post.postType,
                        post.viewCount,
                        new QWriterDto(
                                user.id,
                                user.compressedProfileImgURL,
                                user.nickname,
                                Expressions.asString("")
                        ),
                        post.price,
                        post.createdDate,
                        post.isPurchasable,
                        post.isSold,
                        post.isAnonymous,
                        me == null ? Expressions.asBoolean(false) : post.user.eq(me), //isMyPost
                        Expressions.asBoolean(false), //isLiked
                        Expressions.asBoolean(false) //isBookmarked
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

    @Override
    public List<FeedResultPostDto> searchUserPageBy(SearchFeedConditionDto searchFeedConditionDto, User owner, User me, Post cursorPost) {

        List<Tuple> paginatedResult = queryFactory
                .select(post.id, postLike.post.id.count())
                .from(post)
                .leftJoin(post.postLikeList, postLike)
                .leftJoin(post.bookmarkList, bookmark)
                .leftJoin(post.purchaseList, purchase)
                .where(
                        hiddenPostFilter(owner, me),
                        anonymousPostFilter(owner, me),
                        userPageFeedOption(searchFeedConditionDto.getUserPageFeedOption(), owner, me),
                        cursorPagination(cursorPost, searchFeedConditionDto.getSortStrategy()),
                        sameLevelCursorFilter(cursorPost, searchFeedConditionDto.getSortStrategy())
                )
                .orderBy(boardSort(SortStrategy.CHRONOLOGICAL))
                .groupBy(post.id, postLike.post.id)
                .limit(searchFeedConditionDto.getSize())
                .fetch();

        List<Long> contentIdList = paginatedResult.stream().map(x -> x.get(post.id)).collect(Collectors.toList());

        List<FeedResultPostDto> content = queryFactory
                .select(new QFeedResultPostDto(
                        post.id,
                        post.title,
                        new QLocationDto(
                                post.location.address,
                                post.location.longitude,
                                post.location.latitude,
                                post.location.region_1depth_name,
                                post.location.region_2depth_name
                        ),
                        post.thumbnailImgURL,
                        post.postLikeList.size(),
                        Expressions.asNumber(0),
                        post.postType,
                        post.viewCount,
                        new QWriterDto(
                                user.id,
                                user.compressedProfileImgURL,
                                user.nickname,
                                Expressions.asString("")
                        ),
                        post.price,
                        post.createdDate,
                        post.isPurchasable,
                        post.isSold,
                        post.isAnonymous,
                        me == null ? Expressions.asBoolean(false) : post.user.eq(me), //isMyPost
                        Expressions.asBoolean(false), //isLiked
                        Expressions.asBoolean(false) //isBookmarked
                ))
                .from(post)
                .leftJoin(post.user, user)
                .where(post.id.in(contentIdList))
                .orderBy(boardSort(SortStrategy.CHRONOLOGICAL))
                .fetch();

        for(FeedResultPostDto dto : content) {
            dto.setIsBookmarked(bookmarkRepository.existsByUserAndPostId(me, dto.getPostId()));
            dto.setIsLiked(postLikeRepository.existsByUserAndPostId(me, dto.getPostId()));
        }

        return content;
    }

    private Predicate keyWordSearch(String searchString) {

        if(searchString == null) {
            return null;
        } else {
            return ExpressionUtils.or(post.title.contains(searchString), post.content.contains(searchString));
        }
    }

    private Predicate hashtagSearch(String searchHashtag) {

        if(searchHashtag == null) {
            return null;
        } else {
            return hashtag.content.eq(searchHashtag);
        }
    }

    private Predicate anonymousPostFilter(User owner, User me) {

        if(owner == me) {
            return null;
        } else {
            return post.isAnonymous.eq(false);
        }
    }

    private Predicate hiddenPostFilter(User owner, User me) {

        if(owner == me) {
            return null;
        } else {
            return post.isHidden.eq(false);
        }
    }

    private Predicate userPageFeedOption(UserPageFeedOption userPageFeedOption, User owner, User me) {

        if(userPageFeedOption == null) {
            return null;
        }

        switch (userPageFeedOption) {
            case BUY:
                return purchase.user.eq(me);
            case BOOKMARK:
                return bookmark.user.eq(me);
            case WRITE:
            default:
                return post.user.eq(owner);
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
