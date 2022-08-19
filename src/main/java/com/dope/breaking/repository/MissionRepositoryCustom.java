package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.mission.MissionFeedResponseDto;

import java.util.List;

public interface MissionRepositoryCustom {

    List<MissionFeedResponseDto> searchMissionFeed(User me, Mission cursorMission, Long size);

}
