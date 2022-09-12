package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.mission.MissionFeedResponseDto;
import com.dope.breaking.dto.mission.QMissionFeedResponseDto;
import com.dope.breaking.dto.post.QLocationDto;
import com.dope.breaking.dto.post.QWriterDto;
import com.dope.breaking.service.SearchMissionConditionDto;
import com.dope.breaking.service.SortStrategy;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static com.dope.breaking.domain.post.QMission.mission;
import static com.dope.breaking.domain.user.QUser.user;

public class MissionRepositoryCustomImpl implements MissionRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public MissionRepositoryCustomImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MissionFeedResponseDto> searchMissionFeed(User me, Mission cursorMission, Long size, SearchMissionConditionDto searchMissionConditionDto){

        return queryFactory
                .select(new QMissionFeedResponseDto(
                        mission.id,
                        mission.title,
                        mission.viewCount,
                        mission.startTime,
                        mission.endTime,
                        mission.createdDate,
                        new QLocationDto(
                                mission.location.address,
                                mission.location.longitude,
                                mission.location.latitude,
                                mission.location.region_1depth_name,
                                mission.location.region_1depth_name
                        ),
                        new QWriterDto(
                            mission.user.id,
                            mission.user.compressedProfileImgURL,
                            mission.user.nickname
                        ),
                        me == null ? Expressions.asBoolean(false) : mission.user.eq(me),
                        mission.postList.size()
                ))
                .from(mission)
                .leftJoin(mission.user,user)
                .where(
                        onGoingFilter(searchMissionConditionDto.getIsMissionOnGoing()),
                        cursorPagination(cursorMission)
                )
                .orderBy(mission.id.desc())
                .limit(size)
                .fetch();
    }

    private Predicate onGoingFilter(Boolean isMissionOnGoing) {
        if(isMissionOnGoing == null) {
            return null;
        }
        if(isMissionOnGoing) {
            return ExpressionUtils.and(mission.startTime.loe(LocalDateTime.now()), mission.endTime.goe(LocalDateTime.now()));
        } else {
            return null;
        }
    }

    private Predicate cursorPagination(Mission cursorMission) {
        if(cursorMission == null) {
            return null;
        } else {
            return mission.id.gt(cursorMission.getId());
        }
    }
}
