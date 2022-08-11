package com.dope.breaking.repository;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ForListInfoResponseDto;

import java.util.List;

public interface FollowRepositoryCustom {

    public List<ForListInfoResponseDto> followingList(User me, User viewedUser, Long cursorId, int size);
    public List<ForListInfoResponseDto> followerList(User me, User viewedUser, Long cursorId, int size);

}
