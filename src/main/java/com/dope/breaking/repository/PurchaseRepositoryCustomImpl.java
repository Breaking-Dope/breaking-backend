package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ForListInfoResponseDto;
import com.dope.breaking.dto.user.QForListInfoResponseDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.dope.breaking.domain.financial.QPurchase.purchase;
import static com.dope.breaking.domain.user.QFollow.follow;
import static com.dope.breaking.domain.user.QUser.user;

@Repository
public class PurchaseRepositoryCustomImpl implements PurchaseRepositoryCustom{

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final JPAQueryFactory queryFactory;

    public PurchaseRepositoryCustomImpl(@Lazy FollowRepository followRepository, @Lazy UserRepository userRepository, @Lazy EntityManager em){
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<ForListInfoResponseDto> purchaseList(User me, Post post, Long cursorId, int size){

        List<ForListInfoResponseDto> content = queryFactory
                .select(new QForListInfoResponseDto(
                        purchase.id,
                        purchase.user.id,
                        purchase.user.nickname,
                        purchase.user.statusMsg,
                        purchase.user.compressedProfileImgURL
                ))
                .from(purchase)
                .leftJoin(purchase.user,user)
                .where(
                        cursorPagination(cursorId),
                        purchase.post.eq(post)
                )
                .limit(size)
                .fetch();

        if(me != null) {
            for(ForListInfoResponseDto forListInfoResponseDto : content) {
                forListInfoResponseDto.setFollowing(followRepository.existsFollowsByFollowedAndFollowing(userRepository.findById(forListInfoResponseDto.getUserId()).get(), me));
            }
        }

        return content;
    }

    private Predicate cursorPagination(Long cursorId) {
        if(cursorId == null || cursorId == 0) {
            return null;
        }
        return purchase.id.gt(cursorId);
    }

}
