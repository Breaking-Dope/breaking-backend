package com.dope.breaking.service;


import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;

    public void deleteById(Long followId){
        followRepository.deleteById(followId);
    }

    public Optional<Follow> findById(Long followId) {return followRepository.findById(followId);}

    public Boolean isFollowing (User followingUser, User followedUser) {

        List<Follow> followingList = followingUser.getFollowingList();

        for (Follow follow : followingList) {
            if (follow.getFollowed() == followedUser) {
                return true;
            }
        }
        return false;
    }

    public void AFollowB (User followingUser, User followedUser){

        Follow follow = new Follow();

        follow.updateFollowing(followingUser);
        follow.updateFollowed(followedUser);

        followingUser.getFollowingList().add(follow);
        followedUser.getFollowerList().add(follow);

    }

    public void AUnfollowB (User followingUser, User followedUser){

        for (Follow follow : followingUser.getFollowingList()) {

            if (follow.getFollowed() == followedUser){

                followingUser.getFollowingList().remove(follow);
                followedUser.getFollowerList().remove(follow);
                break;

            }
        }
    }

}
