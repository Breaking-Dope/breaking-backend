package com.dope.breaking.repository;

import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long>, FollowRepositoryCustom {

    //팔로워 수
    int countFollowsByFollowing(User user);

    //팔로잉 수
    int countFollowsByFollowed(User user);

    boolean existsFollowsByFollowedAndFollowing(User followed, User following);

    void deleteByFollowedAndFollowing(User followed, User following);

    boolean existsFollowsByFollowingIdAndFollowedId(Long followed, Long following);
}
