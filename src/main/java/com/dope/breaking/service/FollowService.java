package com.dope.breaking.service;


import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ForListInfoResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.follow.AlreadyFollowingException;
import com.dope.breaking.exception.follow.AlreadyUnfollowingException;
import com.dope.breaking.exception.user.NoSuchUserException;
import com.dope.breaking.repository.FollowRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


@Transactional
@Service
@RequiredArgsConstructor
public class FollowService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public void follow(String username, Long userId){

        User followingUser = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        User followedUser = userRepository.findById(userId).orElseThrow(NoSuchUserException::new);

        if(followRepository.existsFollowsByFollowedAndFollowing(followedUser,followingUser)){
            throw new AlreadyFollowingException();
        }

        Follow follow = new Follow(followingUser,followedUser);
        followRepository.save(follow);


    }

    public void unfollow(String username, Long userId){

        User followingUser = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        User followedUser = userRepository.findById(userId).orElseThrow(NoSuchUserException::new);

        if(!followRepository.existsFollowsByFollowedAndFollowing(followedUser,followingUser)){
            throw new AlreadyUnfollowingException();
        }
        followRepository.deleteByFollowedAndFollowing(followedUser,followingUser);
    }


    public List<ForListInfoResponseDto> followUserList(Principal principal, Long userId, Long cursorId, int size, FollowTargetType followTargetType){

        User me = null;
        if(principal != null){
            me = userRepository.findByUsername(principal.getName()).orElseThrow(InvalidAccessTokenException::new);
        }
        User selectedUser = userRepository.findById(userId).orElseThrow(NoSuchUserException::new);

        if(followTargetType == FollowTargetType.FOLLOWING) {
            return followRepository.followingList(me, selectedUser, cursorId, size);
        }
        else if(followTargetType == FollowTargetType.FOLLOWED) {
            return followRepository.followerList(me, selectedUser, cursorId, size);
        }

        return null;

    }

}
