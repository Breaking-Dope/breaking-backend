package com.dope.breaking.service;


import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.response.MessageResponseDto;
import com.dope.breaking.dto.user.FollowInfoResponseDto;
import com.dope.breaking.exception.oauth.InvalidAccessTokenException;
import com.dope.breaking.exception.user.NoSuchUserException;
import com.dope.breaking.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@Transactional
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserService userService;

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

    public ResponseEntity<?> followingUsers (Long userId){

        User user = userService.findById(userId).orElseThrow(NoSuchUserException::new);

        List<Follow> followingList = user.getFollowingList();
        List<FollowInfoResponseDto> followInfoResponseDtoList = new ArrayList<>();

        for (Follow follow : followingList) {
            User followedUser = follow.getFollowed();
            followInfoResponseDtoList.add (new FollowInfoResponseDto(followedUser.getId(),followedUser.getNickname(),followedUser.getStatusMsg(),followedUser.getProfileImgURL()));
        }

        return ResponseEntity.ok().body(followInfoResponseDtoList);
    }

    public ResponseEntity<?> followerUsers (Long userId){


        User user = userService.findById(userId).orElseThrow(NoSuchUserException::new);


        List<Follow> followerList = user.getFollowerList();
        List<FollowInfoResponseDto> followInfoResponseDtoList = new ArrayList<>();

        for (Follow follow : followerList) {
            User followedUser = follow.getFollowing ();
            followInfoResponseDtoList.add (new FollowInfoResponseDto(followedUser.getId(),followedUser.getNickname(),followedUser.getStatusMsg(),followedUser.getProfileImgURL()));
        }

        return ResponseEntity.ok().body(followInfoResponseDtoList);
    }
}
