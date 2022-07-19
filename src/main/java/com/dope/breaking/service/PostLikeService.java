package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostLike;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ForListInfoResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.like.AlreadyLikedException;
import com.dope.breaking.exception.like.AlreadyUnlikedException;
import com.dope.breaking.exception.post.NoSuchPostException;
import com.dope.breaking.repository.PostLikeRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

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
    public List<ForListInfoResponseDto> likedUserList (Long postId){

        Post post = postRepository.findById(postId).orElseThrow(NoSuchPostException::new);

        List<PostLike> postLikeList = postLikeRepository.findAllByPost(post);
        List<ForListInfoResponseDto> forListInfoResponseDtoList = new ArrayList<>();
        for (PostLike postLike : postLikeList) {
            User user = postLike.getUser();
            forListInfoResponseDtoList.add(new ForListInfoResponseDto(user.getId(),user.getNickname(),user.getStatusMsg(),user.getProfileImgURL()));
        }
        return forListInfoResponseDtoList;
    }
}

