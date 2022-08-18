package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.mission.MissionFeedResponseDto;
import com.dope.breaking.dto.mission.QMissionFeedResponseDto;
import com.dope.breaking.dto.post.QLocationDto;
import com.dope.breaking.dto.post.QWriterDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;

import static com.dope.breaking.domain.post.QMission.mission;
import static com.dope.breaking.domain.post.QPost.post;
import static com.dope.breaking.domain.user.QUser.user;

public class MissionRepositoryCustomImpl implements MissionRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public MissionRepositoryCustomImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MissionFeedResponseDto> searchMissionFeed(User me, Mission cursorMission, int size){

        return queryFactory
                .select(new QMissionFeedResponseDto(
                        mission.id,
                        mission.title,
                        mission.startTime,
                        mission.endTime,
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
                        me == null ? Expressions.asBoolean(false) : mission.user.eq(me)
                ))
                .from(mission)
                .leftJoin(mission.user,user)
                .where(
                        cursorPagination(cursorMission)
                )
                .limit(size)
                .fetch();
    }

    private Predicate cursorPagination(Mission cursorMission) {
        if(cursorMission == null) {
            return null;
        } else {
            return mission.id.gt(cursorMission.getId());
        }
    }
}
