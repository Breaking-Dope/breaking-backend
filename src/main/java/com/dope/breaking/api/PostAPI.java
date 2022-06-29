package com.dope.breaking.api;

import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class PostAPI {

    @Autowired UserRepository userRepository;
    @Autowired PostRepository postRepository;

    @PostMapping("/post/{userId}")
    public PostWriteRequest newPostId(
            @PathVariable ("userId") Long userId,
            @RequestBody PostWriteRequest postWriteRequest){

        return postWriteRequest;

    }

    @Data
    @RequiredArgsConstructor
    public class postId{

        private final Long postId;

    }

    // 1. request body의 정보를 dto를 통해 끌어온다.
    // 2. 해당되는 post를 새로 생성한다. 그외에도 hashtag, media를 생성한다.
    // 3. userId를 통해 userRepository에서 user를 끌어온다.
    // 4. cascade 등을 통해 매핑을 한다.

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PostWriteRequest {

        private String title;
        private String content;
        private int price;
        private Boolean isAnonymous;
        private String postType;

        @JsonFormat(shape = JsonFormat.Shape.SCALAR, pattern = "yyyy-MM-dd HH:mm:ss")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime eventTime;

        private LocationDto location;
        private List<MediaDto> mediaList;
        private List<String> hashtagList;
        //private List<Hashtag> hashtagList;

    }

    @Data
    public static class LocationDto {

        private String region;
        private Double longitude;
        private Double latitude;

    }

    @Data
    public static class MediaDto {
        private String mediaType;   // PHOTO 혹은 VIDEO
        private String mediaURL;
    }

}
