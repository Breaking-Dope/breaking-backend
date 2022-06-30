package com.dope.breaking.service;

import com.dope.breaking.domain.media.MediaType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class MediaServiceTest {

    @Autowired MediaService mediaService;

    @Test
    void findMediaType() {

        String fileName1 = "1.png";
        String fileName2 = "2.mov";

        MediaType mediaType1 = mediaService.findMediaType(fileName1);
        MediaType mediaType2 = mediaService.findMediaType(fileName2);

        Assertions.assertThat(mediaType1).isEqualTo(MediaType.PHOTO);
        Assertions.assertThat(mediaType2).isEqualTo(MediaType.VIDEO);

    }

}