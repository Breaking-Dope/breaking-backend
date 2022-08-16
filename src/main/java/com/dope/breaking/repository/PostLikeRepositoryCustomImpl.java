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

import static com.dope.breaking.domain.post.QPostLike.postLike;
import static com.dope.breaking.domain.user.QUser.user;


@Repository
public class PostLikeRepositoryCustomImpl implements  PostLikeRepositoryCustom{

    private final UserRepository userRepository;
    private final JPAQueryFactory queryFactory;
    private final FollowRepository followRepository;

    public PostLikeRepositoryCustomImpl(@Lazy UserRepository userRepository, @Lazy EntityManager em, @Lazy FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.queryFactory = new JPAQueryFactory(em);
        this.followRepository = followRepository;
    }

    @Override
    public List<ForListInfoResponseDto> postLikeList(User me, Post post, Long cursorId, int size){

        List<ForListInfoResponseDto> content = queryFactory
                .select(new QForListInfoResponseDto(
                        postLike.id,
                        postLike.user.id,
                        postLike.user.nickname,
                        postLike.user.statusMsg,
                        postLike.user.compressedProfileImgURL
                ))
                .from(postLike)
                .leftJoin(postLike.user,user)
                .where(
                        cursorPagination(cursorId),
                        postLike.post.eq(post)
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
        return postLike.id.gt(cursorId);
    }

}
