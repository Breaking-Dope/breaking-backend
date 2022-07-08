package com.dope.breaking.service;

import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostType;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.PostRequestDto;
import com.dope.breaking.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;

    private final UserService userService;

    private final MediaService mediaService;


    @Transactional
    public Long create(String username, PostRequestDto postRequestDto, List<MultipartFile> files) throws Exception {
        Long postid = null; // null로 초기화
        PostType postType = null;
        if (PostType.EXCLUSIVE.getTitle().equals(postRequestDto.getPostType())) {
            postType = PostType.EXCLUSIVE;
        } else if (PostType.CHARGED.getTitle().equals(postRequestDto.getPostType())) {
            postType = PostType.CHARGED;
        } else if (PostType.FREE.getTitle().equals(postRequestDto.getPostType())) {
            postType = PostType.FREE;
        }
        Post post = new Post();
        try {
            post = Post.builder()
                    .title(postRequestDto.getTitle())
                    .content(postRequestDto.getContent())
                    .postType(postType)
                    .location(Location.builder()
                            .region(postRequestDto.getLocationDto().getRegion())
                            .latitude(postRequestDto.getLocationDto().getLatitude())
                            .longitude(postRequestDto.getLocationDto().getLongitude()).build())
                    .eventTime(postRequestDto.getEventTime())
                    .isAnonymous(postRequestDto.getIsAnonymous())
                    .price(postRequestDto.getPrice())
                    .build();

            User user = userService.findByUsername(username).get();
            post.setUser(user);
            postid = postRepository.save(post).getId();

        } catch (Exception e) {
            log.info("게시글 entity화 실패.");
            throw e;
        }
        Map<String, List<String>> map = new LinkedHashMap<>();
        if (!files.isEmpty() && files.get(0).getSize() != 0) {//사용자가 파일을 보내지 않아도 기본적으로 갯수는 1로 반영되며, byte는 0으로 반환된다. 따라서 파일이 확실히 존재할때만 DB에 반영되도록 함.
            try {
                map = mediaService.uploadMediaAndThumbnail(files, postRequestDto.getThumbnailIndex());
            } catch (Exception e) {
                log.info("파일이 정상적으로 처리되지 않음");
                log.info(e.toString());
                throw e;
            }
            mediaService.createMediaEntities(map.get("mediaList"), post);
            post.setThumbnailImgURL(map.get("thumbnail").get(0).toString());
        }
        return postid;
    }

    @Transactional
    public void modify(long postid, PostRequestDto postRequestDto, List<MultipartFile> files) throws Exception {
        PostType postType = null;
        if (PostType.EXCLUSIVE.getTitle().equals(postRequestDto.getPostType())) {
            postType = PostType.EXCLUSIVE;
        } else if (PostType.CHARGED.getTitle().equals(postRequestDto.getPostType())) {
            postType = PostType.CHARGED;
        } else if (PostType.FREE.getTitle().equals(postRequestDto.getPostType())) {
            postType = PostType.FREE;
        }
        Post modifypost = postRepository.getById(postid);
        try {
            Location location = Location.builder()
                    .region(postRequestDto.getLocationDto().getRegion())
                    .latitude(postRequestDto.getLocationDto().getLatitude())
                    .longitude(postRequestDto.getLocationDto().getLongitude()).build();

            modifypost.UpdatePost(postRequestDto.getTitle(), postRequestDto.getContent(), postType, location, postRequestDto.getPrice(), postRequestDto.getIsAnonymous(), postRequestDto.getEventTime());
        } catch (Exception e) {
            log.info("게시글 수정 실패. PostId : {}", postid);
            throw e;
        }

        Map<String, List<String>> map = new LinkedHashMap<>();
        List<String> preImageUrl = mediaService.preMediaURL(postid);
        if (!files.isEmpty() && files.get(0).getSize() != 0) {//사용자가 파일을 보내지 않아도 기본적으로 갯수는 1로 반영되며, byte는 0으로 반환된다. 따라서 파일이 확실히 존재할때만 DB에 반영되도록 함.
            try {
                map = mediaService.uploadMediaAndThumbnail(files, postRequestDto.getThumbnailIndex());
                log.info(files.get(postRequestDto.getThumbnailIndex()).getOriginalFilename());
            } catch (Exception e) {
                log.info("파일이 정상적으로 처리되지 않음");
                log.info(e.toString());
                throw e;
            }
            log.info(map.get("mediaList").toString());
        }
        mediaService.modifyMediaEnities(preImageUrl, map.get("mediaList"), postid);
        log.info(preImageUrl.toString());
        mediaService.deletePostMedias(preImageUrl);
        mediaService.DeleteThumbnailImg(modifypost.getThumbnailImgURL()); //기존 썸넹리 사진은 삭제한다.
        modifypost.setThumbnailImgURL(map.get("thumbnail").get(0).toString());

    }


    public boolean existByPostIdAndUserId(long postid, long userid) {
        return postRepository.existsByIdAndUserId(postid, userid);
    }

}
