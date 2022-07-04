package com.dope.breaking.service;

import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostType;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.PostCreateRequestDto;
import com.dope.breaking.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;

    private final UserService userService;

    private final MediaService mediaService;


    @Transactional
    public Long create(String username, PostCreateRequestDto postCreateRequestDto, List<MultipartFile> files) {
        Long postid = null; // null로 초기

        PostType postType = null;
        if (PostType.EXCLUSIVE.getTitle().equals(postCreateRequestDto.getPostType())) {
            postType = PostType.EXCLUSIVE;
        } else if (PostType.CHARGED.getTitle().equals(postCreateRequestDto.getPostType())) {
            postType = PostType.CHARGED;
        } else if (PostType.FREE.getTitle().equals(postCreateRequestDto.getPostType())) {
            postType = PostType.FREE;
        }
        Post post = new Post();
        try {
            post = Post.builder()
                    .title(postCreateRequestDto.getTitle())
                    .content(postCreateRequestDto.getContent())
                    .postType(postType)
                    .location(Location.builder()
                            .region(postCreateRequestDto.getLocationDto().getRegion())
                            .latitude(postCreateRequestDto.getLocationDto().getLatitude())
                            .longitude(postCreateRequestDto.getLocationDto().getLongitude()).build())
                    .eventTime(postCreateRequestDto.getEventTime())
                    .isAnonymous(postCreateRequestDto.getIsAnonymous())
                    .price(postCreateRequestDto.getPrice())
                    .build();
            User user = userService.findByUsername(username).get();
            post.setUser(user);
            postid = postRepository.save(post).getId();
        } catch (Exception e) {
            log.info("게시글 entity화 실패.");
            throw e;
        }

        List<String> filename = new LinkedList<>();
        if (!files.isEmpty() && files.get(0).getSize() != 0) {//사용자가 파일을 보내지 않아도 기본적으로 갯수는 1로 반영되며, byte는 0으로 반환된다. 따라서 파일이 확실히 존재할때만 DB에 반영되도록 함.
            try {
                filename = mediaService.uploadMedias(files);

            } catch (Exception e) {
                log.info("파일이 정상적으로 처리되지 않음");
                log.info(e.toString());
            }
            mediaService.createMediaEntities(filename, post);
        }


        return postid;
    }


}
