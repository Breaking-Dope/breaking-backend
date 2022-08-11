package com.dope.breaking.repository;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ForListInfoResponseDto;
import com.dope.breaking.dto.user.QForListInfoResponseDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.dope.breaking.domain.user.QFollow.follow;
import static com.dope.breaking.domain.user.QUser.user;

@Repository
public class FollowRepositoryCustomImpl implements FollowRepositoryCustom{

    private final UserRepository userRepository;
    private final JPAQueryFactory queryFactory;
    private final FollowRepository followRepository;

    public FollowRepositoryCustomImpl(@Lazy UserRepository userRepository, @Lazy EntityManager em, @Lazy FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.queryFactory = new JPAQueryFactory(em);
        this.followRepository = followRepository;
    }

    @Override
    public List<ForListInfoResponseDto> followingList(User me, User viewedUser, Long cursorId, int size){

        List<ForListInfoResponseDto> content = queryFactory
                .select(new QForListInfoResponseDto(
                        follow.id,
                        follow.followed.id,
                        follow.followed.nickname,
                        follow.followed.statusMsg,
                        follow.followed.compressedProfileImgURL
                ))
                .from(follow)
                .leftJoin(follow.following,user)
                .leftJoin(follow.followed,user)
                .where(
                        cursorPagination(cursorId),
                        follow.following.eq(viewedUser)
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

    @Override
    public List<ForListInfoResponseDto> followerList(User me, User viewedUser, Long cursorId, int size){

        List<ForListInfoResponseDto> content = queryFactory
                .select(new QForListInfoResponseDto(
                        follow.id,
                        follow.following.id,
                        follow.following.nickname,
                        follow.following.statusMsg,
                        follow.following.compressedProfileImgURL
                ))
                .from(follow)
                .leftJoin(follow.following,user)
                .leftJoin(follow.followed,user)
                .where(
                        cursorPagination(cursorId),
                        follow.followed.eq(viewedUser)
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
        return follow.id.gt(cursorId);
    }

}
