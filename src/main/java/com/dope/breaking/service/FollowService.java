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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Transactional
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

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

    public void follow(User followingUser, User followedUser){

        Follow follow = new Follow();

        follow.updateFollowing(followingUser);
        follow.updateFollowed(followedUser);

        followingUser.getFollowingList().add(follow);
        followedUser.getFollowerList().add(follow);

    }

    public void unfollow(User followingUser, User followedUser){

        for (Follow follow : followingUser.getFollowingList()) {

            if (follow.getFollowed() == followedUser){

                followingUser.getFollowingList().remove(follow);
                followedUser.getFollowerList().remove(follow);
                break;

            }
        }
    }

    public void followUser(String username, Long userId) {

        User followingUser = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        User followedUser = userRepository.findById(userId).orElseThrow(NoSuchUserException::new);

        if (isFollowing(followingUser,followedUser)){
            throw new AlreadyFollowingException();
        }

        follow(followingUser,followedUser);
        userRepository.save(followingUser);
        userRepository.save(followedUser);

    }

    public void unfollowUser(String username, Long userId) {

        User followingUser = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        User followedUser = userRepository.findById(userId).orElseThrow(NoSuchUserException::new);

        if (!isFollowing(followingUser,followedUser)){
            throw new AlreadyUnfollowingException();
        }

        unfollow(followingUser,followedUser);
        userRepository.save(followingUser);
        userRepository.save(followedUser);

    }

    public List<ForListInfoResponseDto> followingUsers (Long userId){

        User user = userRepository.findById(userId).orElseThrow(NoSuchUserException::new);

        List<Follow> followingList = user.getFollowingList();
        List<ForListInfoResponseDto> forListInfoResponseDtoList = new ArrayList<>();

        for (Follow follow : followingList) {
            User followedUser = follow.getFollowed();
            forListInfoResponseDtoList.add (new ForListInfoResponseDto(followedUser.getId(),followedUser.getNickname(),followedUser.getStatusMsg(),followedUser.getCompressedProfileImgURL()));
        }

        return forListInfoResponseDtoList;

    }

    public List<ForListInfoResponseDto> followerUsers (Long userId){

        User user = userRepository.findById(userId).orElseThrow(NoSuchUserException::new);

        List<Follow> followerList = user.getFollowerList();
        List<ForListInfoResponseDto> forListInfoResponseDtoList = new ArrayList<>();

        for (Follow follow : followerList) {
            User followedUser = follow.getFollowing();
            forListInfoResponseDtoList.add(new ForListInfoResponseDto(followedUser.getId(),followedUser.getNickname(),followedUser.getStatusMsg(),followedUser.getCompressedProfileImgURL()));
        }

        return forListInfoResponseDtoList;
    }
}
