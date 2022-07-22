package com.dope.breaking.service;


import com.dope.breaking.domain.hashtag.Hashtag;
import com.dope.breaking.domain.hashtag.HashtagType;
import com.dope.breaking.domain.hashtag.PostCommentHashtag;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.repository.HashtagRepository;
import com.dope.breaking.repository.PostCommentHashtagRepository;
import com.dope.breaking.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostCommentHashtagService {

    private final PostCommentHashtagRepository postCommentHashtagRepository;

    private final PostRepository postRepository;

    private final HashtagRepository hashtagRepository;


    @Transactional
    public void savePostCommentHashtag(List<String> postHashtags, Long postId, HashtagType hashtagType){
        Post post = postRepository.findById(postId).get();

        for(String hashtag : postHashtags){
            Hashtag hashtagEntity = new Hashtag();
            if(hashtagRepository.existsByHashtag(hashtag)){
                hashtagEntity = hashtagRepository.findByHashtag(hashtag);
            }
            else{
                hashtagEntity = hashtagRepository.save(Hashtag.builder().hashtag(hashtag).build());
            }
            PostCommentHashtag postCommentHashtag = PostCommentHashtag.builder()
                    .hashtag(hashtagEntity)
                    .post(post)
                    .comment(null)
                    .hashtagType(hashtagType)
                    .build();
            postCommentHashtagRepository.save(postCommentHashtag);
        }
    }

    @Transactional
    public void modifyPostCommentHashtag(List<String> postHashtags, Post post, HashtagType hashtagType){
        postCommentHashtagRepository.deleteAllByPost(post);
        for(String hashtag : postHashtags){
            Hashtag hashtagEntity = new Hashtag();
            if(hashtagRepository.existsByHashtag(hashtag)){
                hashtagEntity = hashtagRepository.findByHashtag(hashtag);
            }
            else{
                hashtagEntity = hashtagRepository.save(Hashtag.builder().hashtag(hashtag).build());
            }
            PostCommentHashtag postCommentHashtag = PostCommentHashtag.builder()
                    .hashtag(hashtagEntity)
                    .comment(null)
                    .hashtagType(hashtagType)
                    .post(post).build();
            postCommentHashtagRepository.save(postCommentHashtag);
        }
    }





}
