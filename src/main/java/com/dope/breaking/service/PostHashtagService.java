package com.dope.breaking.service;


import com.dope.breaking.domain.hashtag.Hashtag;
import com.dope.breaking.domain.hashtag.PostHashtag;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.repository.HashtagRepository;
import com.dope.breaking.repository.PostHashtagRepository;
import com.dope.breaking.repository.PostRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostHashtagService {

    private final PostHashtagRepository postHashtagRepository;

    private final PostRepository postRepository;

    private final HashtagRepository hashtagRepository;





    @Transactional
    public void savePostHashtag(List<String> postHashtags, Long postId){
        Post post = postRepository.findById(postId).get();

        for(String hashtag : postHashtags){
            Hashtag hashtagEntity = new Hashtag();
            if(hashtagRepository.existsByHashtag(hashtag)){
                hashtagEntity = hashtagRepository.findByHashtag(hashtag);
            }
            else{
                hashtagEntity = hashtagRepository.save(Hashtag.builder().hashtag(hashtag).build());
            }
            PostHashtag postHashtag = PostHashtag.builder()
                    .hashtag(hashtagEntity)
                    .post(post).build();
            postHashtagRepository.save(postHashtag);
        }
    }

    @Transactional
    public void modifyPostHashtag(List<String> postHashtags, Post post){
        postHashtagRepository.deleteAllByPost(post);
        for(String hashtag : postHashtags){
            Hashtag hashtagEntity = new Hashtag();
            if(hashtagRepository.existsByHashtag(hashtag)){
                hashtagEntity = hashtagRepository.findByHashtag(hashtag);
            }
            else{
                hashtagEntity = hashtagRepository.save(Hashtag.builder().hashtag(hashtag).build());
            }
            PostHashtag postHashtag = PostHashtag.builder()
                    .hashtag(hashtagEntity)
                    .post(post).build();
            postHashtagRepository.save(postHashtag);
        }
    }





}
