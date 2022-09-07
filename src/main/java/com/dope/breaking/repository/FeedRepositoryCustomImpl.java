package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostType;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.*;
import com.dope.breaking.service.SoldOption;
import com.dope.breaking.service.SortStrategy;
import com.dope.breaking.service.UserPageFeedOption;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static com.dope.breaking.domain.financial.QPurchase.purchase;
import static com.dope.breaking.domain.hashtag.QHashtag.*;
import static com.dope.breaking.domain.post.QPost.post;
import static com.dope.breaking.domain.user.QBookmark.bookmark;

@Repository
public class FeedRepositoryCustomImpl implements FeedRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public FeedRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    private JPQLQuery<FeedResultPostDto> getBaseQuery(SearchFeedConditionDto searchFeedConditionDto, Post cursorPost, User me) {
        return queryFactory
                .select(Projections.constructor(FeedResultPostDto.class,
                        post.id,
                        post.title,
                        Projections.constructor(LocationDto.class,
                                post.location.address,
                                post.location.longitude,
                                post.location.latitude,
                                post.location.region_1depth_name,
                                post.location.region_2depth_name
                        ),
                        post.thumbnailImgURL,
                        post.postLikeList.size(),
                        post.commentList.size(),
                        post.postType,
                        post.viewCount,
                        Projections.constructor(WriterDto.class,
                                post.user.id,
                                post.user.compressedProfileImgURL,
                                post.user.nickname
                        ),
                        post.price,
                        post.createdDate,
                        post.isPurchasable,
                        post.isSold,
                        post.isAnonymous,
                        post.isHidden,
                        me == null ? Expressions.asBoolean(false) : post.user.eq(me), //isMyPost
                        Expressions.asBoolean(false), //isLiked
                        Expressions.asBoolean(false) //isBookmarked
                        ))
                .from(post)
                .where(
                        soldOption(searchFeedConditionDto.getSoldOption()),
                        postTypeFilter(searchFeedConditionDto.getPostType()),
                        cursorPagination(cursorPost, searchFeedConditionDto.getSortStrategy()),
                        sameLevelCursorFilter(cursorPost, searchFeedConditionDto.getSortStrategy())
                )
                .limit(searchFeedConditionDto.getSize());
    }

    @Override
    public List<FeedResultPostDto> searchFeedBy(SearchFeedConditionDto searchFeedConditionDto, Post cursorPost, User me) {
        return getBaseQuery(searchFeedConditionDto, cursorPost, me)
                .where(
                        post.isHidden.eq(false),
                        keywordSearch(searchFeedConditionDto.getSearchKeyword()),
                        hashtagSearch(searchFeedConditionDto.getSearchHashtag()),
                        period(searchFeedConditionDto.getDateFrom(), searchFeedConditionDto.getDateTo())
                )
                .orderBy(boardSort(searchFeedConditionDto.getSortStrategy()), boardSort(SortStrategy.CHRONOLOGICAL))
                .fetch();
    }

    @Override
    public List<FeedResultPostDto> searchUserPageBy(SearchFeedConditionDto searchFeedConditionDto, User owner, User me, Post cursorPost) {
        return getBaseQuery(searchFeedConditionDto, cursorPost, me)
                .where(
                        hiddenPostFilter(owner, me),
                        anonymousPostFilter(owner, me),
                        userPageFeedOption(searchFeedConditionDto.getUserPageFeedOption(), owner, me)
                )
                .orderBy(boardSort(SortStrategy.CHRONOLOGICAL))
                .fetch();
    }

    @Override
    public List<FeedResultPostDto> searchFeedByMission(SearchFeedConditionDto searchFeedConditionDto, Mission mission, Post cursorPost, User me) {
        return getBaseQuery(searchFeedConditionDto, cursorPost, me)
                .where(
                        post.mission.eq(mission),
                        hiddenPostFilter(mission, me)
                )
                .orderBy(post.id.desc())
                .fetch();
    }

    private Predicate keywordSearch(String searchString) {

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
            List<Long> postIdList = queryFactory
                    .select(hashtag.post.id)
                    .from(hashtag)
                    .where(
                            hashtag.content.eq(searchHashtag)
                    )
                    .groupBy(hashtag.post.id)
                    .fetch();
            return post.id.in(postIdList);
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

    private Predicate hiddenPostFilter(Mission mission, User me) {

        if(mission.getUser() == me) {
            return null;
        } else {
            return post.isHidden.eq(false);
        }
    }

    private Predicate postTypeFilter(PostType postType) {

        if(postType == null) {
            return null;
        } else {
            return post.postType.eq(postType);
        }
    }

    private Predicate userPageFeedOption(UserPageFeedOption userPageFeedOption, User owner, User me) {

        if(userPageFeedOption == null) {
            return null;
        }

        switch (userPageFeedOption) {
            case BUY:
                List<Long> purchaseIdList = queryFactory
                        .select(purchase.post.id)
                        .from(purchase)
                        .where(
                                purchase.user.eq(me)
                        )
                        .groupBy(purchase.post.id)
                        .fetch();
                return post.id.in(purchaseIdList);

            case BOOKMARK:
                List<Long> bookmarkIdList = queryFactory
                        .select(bookmark.post.id)
                        .from(bookmark)
                        .where(
                                bookmark.user.eq(me)
                        )
                        .groupBy(bookmark.post.id)
                        .fetch();
                return post.id.in(bookmarkIdList);

            case WRITE:
            default:
                return post.user.eq(owner);
        }
    }

    private Predicate soldOption(SoldOption soldOption) {

        if(soldOption == null) {
            return null;
        }

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
