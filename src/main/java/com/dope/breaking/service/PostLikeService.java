package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostLike;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ForListInfoResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.like.AlreadyLikedException;
import com.dope.breaking.exception.like.AlreadyUnlikedException;
import com.dope.breaking.exception.pagination.InvalidCursorException;
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
    public List<ForListInfoResponseDto> postLikeList (String username, Long postId, Long cursorId, int size){

        User me = null;
        if(username != null){
            me = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        }

        Post post = postRepository.findById(postId).orElseThrow(NoSuchPostException::new);

        if(cursorId != null && cursorId != 0L){
           if(!postLikeRepository.existsById(cursorId)){
               throw new InvalidCursorException();
           }
           if(postLikeRepository.getById(cursorId).getPost()!=post){
               throw new InvalidCursorException();
           }
       }

        return postLikeRepository.postLikeList(me,post,cursorId,size);

    }

}

