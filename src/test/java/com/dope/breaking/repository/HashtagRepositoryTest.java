package com.dope.breaking.repository;

import com.dope.breaking.domain.hashtag.Hashtag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class HashtagRepositoryTest {

    @Autowired
    HashtagRepository hashtagRepository;


    @Test
    void existsByHashtag() {
        //Given
        Hashtag hashtag = Hashtag.builder()
                .hashtag("hello1").build();

        //When
        hashtagRepository.save(hashtag);
        Boolean exist = hashtagRepository.existsByHashtag("hello1");

        //Then
        Assertions.assertTrue(exist);
    }

    @Test
    void findByHashtag() {
        //Given
        Hashtag hashtag = Hashtag.builder()
                .hashtag("hello1").build();

        //When
        hashtagRepository.save(hashtag);
        Hashtag result = hashtagRepository.findByHashtag("hello1");

        //Then
        Assertions.assertEquals("hello1", result.getHashtag());

    }
}