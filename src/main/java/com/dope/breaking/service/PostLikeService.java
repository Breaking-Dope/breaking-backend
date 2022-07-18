package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostLike;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.like.AlreadyLikedException;
import com.dope.breaking.exception.like.AlreadyUnlikedException;
import com.dope.breaking.exception.post.NoSuchPostException;
import com.dope.breaking.repository.PostLikeRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public void likePost(User user, Post post){

        if (postLikeRepository.existsPostLikesByUserAndPost(user, post)){
            throw new AlreadyLikedException();
        }

        PostLike postLike =  new PostLike();

        postLike.updateUser(user);
        postLike.updatePost(post);

        user.getPostLikeList().add(postLike);
        post.getPostLikeList().add(postLike);

    }

    public void unlikePost(User user, Post post){

        if (!postLikeRepository.existsPostLikesByUserAndPost(user, post)){
            throw new AlreadyUnlikedException();
        }

        PostLike postLike = postLikeRepository.findPostLikeByUserAndPost(user,post).get();
        user.getPostLikeList().remove(postLike);
        post.getPostLikeList().remove(postLike);

    }

    public void likePostById (String username, Long postId){

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Post post = postRepository.findById(postId).orElseThrow(NoSuchPostException::new);

        likePost(user,post);

        userRepository.save(user);
        postRepository.save(post);

    }

    public void unlikePostById (String username, Long postId){

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Post post = postRepository.findById(postId).orElseThrow(NoSuchPostException::new);

        unlikePost(user,post);

        userRepository.save(user);
        postRepository.save(post);

    }

}
