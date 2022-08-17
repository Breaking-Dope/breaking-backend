package com.dope.breaking.repository;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.QSearchUserResponseDto;
import com.dope.breaking.dto.user.SearchUserResponseDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.dope.breaking.domain.comment.QComment.comment;
import static com.dope.breaking.domain.user.QFollow.follow;
import static com.dope.breaking.domain.user.QUser.user;

@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public UserRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<SearchUserResponseDto> searchUserBy(User me, String searchKeyword, User cursorUser, Long size) {

        return queryFactory
                .select(new QSearchUserResponseDto(
                        user.id,
                        user.compressedProfileImgURL,
                        user.nickname,
                        user.email,
                        user.statusMsg,
                        user.role,
                        user.followerList.size(),
                        Expressions.asBoolean(false)
                ))
                .from(user)
                .where(
                        cursorPagination(cursorUser),
                        user.nickname.contains(searchKeyword)
                )
                .limit(size)
                .fetch();
    }

    private Predicate cursorPagination(User cursorUser) {
        if(cursorUser == null) {
            return null;
        } else {
            return user.id.gt(cursorUser.getId());
        }
    }
}
