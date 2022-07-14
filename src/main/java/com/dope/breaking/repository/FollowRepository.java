package com.dope.breaking.repository;

import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    //팔로워 수
    int countFollowsByFollowing(User user);

    //팔로잉 수
    int countFollowsByFollowed(User user);

}
