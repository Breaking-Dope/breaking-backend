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
import java.util.Optional;


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

    public List<ForListInfoResponseDto> followingUsers (Principal principal, Long userId){

        User selectedUser = userRepository.findById(userId).orElseThrow(NoSuchUserException::new);

        List<Follow> followingList = followRepository.findAllByFollowing(selectedUser);
        List<ForListInfoResponseDto> forListInfoResponseDtoList = new ArrayList<>();

        if (principal == null) {
            for (Follow follow : followingList) {
                User followedUser = follow.getFollowed();
                forListInfoResponseDtoList.add(new ForListInfoResponseDto(followedUser.getId(), followedUser.getNickname(), followedUser.getStatusMsg(), followedUser.getCompressedProfileImgURL(), false));
            }
        }
        else{
            User user = userRepository.findByUsername(principal.getName()).orElseThrow(InvalidAccessTokenException::new);
            for (Follow follow : followingList) {
                User followedUser = follow.getFollowed();
                boolean isFollowing = followRepository.existsFollowsByFollowedAndFollowing(followedUser, user);
                forListInfoResponseDtoList.add(new ForListInfoResponseDto(followedUser.getId(), followedUser.getNickname(), followedUser.getStatusMsg(), followedUser.getCompressedProfileImgURL(), isFollowing));
            }
        }

        return forListInfoResponseDtoList;

    }

    public List<ForListInfoResponseDto> followerUsers (Principal principal, Long userId){

        User selectedUser = userRepository.findById(userId).orElseThrow(NoSuchUserException::new);

        List<Follow> followerList = followRepository.findAllByFollowed(selectedUser);
        List<ForListInfoResponseDto> forListInfoResponseDtoList = new ArrayList<>();

        if (principal == null) {
            for (Follow follow : followerList) {
                User followingUser = follow.getFollowing();
                forListInfoResponseDtoList.add(new ForListInfoResponseDto(followingUser.getId(), followingUser.getNickname(), followingUser.getStatusMsg(), followingUser.getCompressedProfileImgURL(), false));
            }
        }
        else{
            User user = userRepository.findByUsername(principal.getName()).orElseThrow(InvalidAccessTokenException::new);
            for (Follow follow : followerList) {
                User followingUser = follow.getFollowing();
                boolean isFollowing = followRepository.existsFollowsByFollowedAndFollowing(followingUser, user);
                forListInfoResponseDtoList.add(new ForListInfoResponseDto(followingUser.getId(), followingUser.getNickname(), followingUser.getStatusMsg(), followingUser.getCompressedProfileImgURL(), isFollowing));
            }
        }

        return forListInfoResponseDtoList;

    }

}
