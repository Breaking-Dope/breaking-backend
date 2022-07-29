package com.dope.breaking.service;

import com.dope.breaking.domain.media.MediaType;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.repository.PostRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;


@SpringBootTest
@Transactional
class MediaServiceTest {

    @Autowired MediaService mediaService;
    @Autowired PostRepository postRepository;

    @Test
    void findMediaType() {

        String fileName1 = "1.png";
        String fileName2 = "2.mov";

        MediaType mediaType1 = mediaService.findMediaType(fileName1);
        MediaType mediaType2 = mediaService.findMediaType(fileName2);

        Assertions.assertThat(mediaType1).isEqualTo(MediaType.PHOTO);
        Assertions.assertThat(mediaType2).isEqualTo(MediaType.VIDEO);

    }

    @Test
    void createMediaEntities(){
        List<String> fileNameList = Arrays.asList("1.png","2.mov","3.jpeg");
        Post post = new Post();
        postRepository.save(post);
        mediaService.createMediaEntities(fileNameList,post);
    }

}