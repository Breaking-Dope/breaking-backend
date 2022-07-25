package com.dope.breaking.service;


import com.dope.breaking.domain.hashtag.Hashtag;
import com.dope.breaking.repository.HashtagRepository;
import com.dope.breaking.repository.PostCommentHashtagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    private final PostCommentHashtagRepository postCommentHashtagRepository;


    public boolean existRelatedHashtag(Hashtag hashtag){
        log.info(Boolean.toString(postCommentHashtagRepository.existsByHashtag(hashtag)));
        if(postCommentHashtagRepository.existsByHashtag(hashtag) == true) {
            return true;
        }
        else{
            return false;
        }
    }

    @Transactional
    public void deleteOrphanHashtag(List<String> hashtags){
        for(String hashtag : hashtags) {
            Hashtag hashtagEntity = hashtagRepository.findByHashtag(hashtag);
            if (!existRelatedHashtag(hashtagEntity)) {
                hashtagRepository.delete(hashtagEntity);
            }
        }
    }
}
