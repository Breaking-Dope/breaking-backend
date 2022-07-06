package com.dope.breaking.repository;

import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.QFeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedRequestDto;
import com.dope.breaking.service.SortStrategy;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

import static com.dope.breaking.domain.post.QPost.post;
import static com.dope.breaking.domain.post.QPostLike.*;
import static com.dope.breaking.domain.user.QUser.user;

public class FeedRepositoryImpl implements FeedRepositoryCustom {

    @Autowired EntityManager em;
    private final JPAQueryFactory queryFactory;

    public FeedRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<FeedResultPostDto> searchDefaultFeedBy(SearchFeedRequestDto searchFeedRequestDto, Pageable pageable) {

        List<Tuple> filteredPaginatedResult = queryFactory
                .select(post.id, postLike.post.id.count())
                .from(post)
                .leftJoin(post.postLikeList, postLike)
                .where( // 세부필터 동적 쿼리 함수로 구현하여 추가
                        post.isHidden.eq(false)
                )
                .groupBy(post.id)
                .orderBy(boardSort(searchFeedRequestDto.getSortStrategy()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Long> contentIdList = filteredPaginatedResult.stream().map(x -> x.get(post.id)).collect(Collectors.toList());

        List<FeedResultPostDto> content = queryFactory
                .select(new QFeedResultPostDto(
                        post.id,
                        post.title,
                        post.location.region,
                        post.thumbnailImgURL,
                        Expressions.asNumber(0),
                        post.postType,
                        post.isSold,
                        post.viewCount,
                        user.id,
                        user.profileImgURL,
                        user.realName,
                        Expressions.asBoolean(false),
                        Expressions.asBoolean(false)
                ))
                .from(post)
                .leftJoin(post.user, user)
                .where(post.id.in(contentIdList))
                .orderBy(post.id.desc())
                .fetch();

        return new PageImpl<>(content, pageable, content.size());
    }

    @Override
    public Page<FeedResultPostDto> searchFilteredFeedBy(SearchFeedRequestDto searchFeedRequestDto, Pageable pageable) {
        return null;
    }

    private OrderSpecifier<?> boardSort(SortStrategy sortStrategy) {

        if(sortStrategy == null){
            return new OrderSpecifier(Order.DESC, post.id);
        }

        switch (sortStrategy){
            case LIKE:
                return new OrderSpecifier(Order.DESC, postLike.post.id.count());
            case VIEW:
                return new OrderSpecifier(Order.DESC, post.viewCount);
            case CHRONOLOGICAL:
                return new OrderSpecifier(Order.DESC, post.id);
        }

        return new OrderSpecifier(Order.DESC, post.id);
    }

}
