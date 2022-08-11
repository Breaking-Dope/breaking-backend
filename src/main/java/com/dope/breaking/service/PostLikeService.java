package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostLike;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ForListInfoResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.like.AlreadyLikedException;
import com.dope.breaking.exception.like.AlreadyUnlikedException;
import com.dope.breaking.exception.post.NoSuchPostException;
import com.dope.breaking.repository.FollowRepository;
import com.dope.breaking.repository.PostLikeRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FollowRepository followRepository;

    @Transactional
    public void likePost(User user, Post post){

        if (postLikeRepository.existsPostLikesByUserAndPost(user, post)){
            throw new AlreadyLikedException();
        }

        PostLike postLike =  new PostLike(user, post);
        postLikeRepository.save(postLike);

    }

    @Transactional
    public void likePostById (String username, Long postId){

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Post post = postRepository.findById(postId).orElseThrow(NoSuchPostException::new);

        likePost(user,post);

    }

    @Transactional
    public void unlikePost(User user, Post post){

        if (!postLikeRepository.existsPostLikesByUserAndPost(user, post)){
            throw new AlreadyUnlikedException();
        }

        postLikeRepository.deleteByUserAndPost(user,post);

    }

    @Transactional
    public void unlikePostById (String username, Long postId){

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Post post = postRepository.findById(postId).orElseThrow(NoSuchPostException::new);

        unlikePost(user,post);

    }

    @Transactional
    public List<ForListInfoResponseDto> likedUserList (Principal principal, Long postId){

        Post post = postRepository.findById(postId).orElseThrow(NoSuchPostException::new);

        List<PostLike> postLikeList = postLikeRepository.findAllByPost(post);
        List<ForListInfoResponseDto> forListInfoResponseDtoList = new ArrayList<>();

        if(principal == null){
            for (PostLike postLike : postLikeList) {
                User likedUser = postLike.getUser();
                forListInfoResponseDtoList.add(new ForListInfoResponseDto(null, likedUser.getId(),likedUser.getNickname(),likedUser.getStatusMsg(),likedUser.getOriginalProfileImgURL(),false ));
            }
        }
        else{
            User user = userRepository.findByUsername(principal.getName()).orElseThrow(InvalidAccessTokenException::new);
            for (PostLike postLike : postLikeList) {
                User likedUser = postLike.getUser();
                boolean isFollowing = followRepository.existsFollowsByFollowedAndFollowing(likedUser,user);
                forListInfoResponseDtoList.add(new ForListInfoResponseDto(null, likedUser.getId(),likedUser.getNickname(),likedUser.getStatusMsg(),likedUser.getOriginalProfileImgURL(),isFollowing));
            }
        }

        return forListInfoResponseDtoList;
    }

}

