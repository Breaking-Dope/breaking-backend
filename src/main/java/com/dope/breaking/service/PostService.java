package com.dope.breaking.service;

import com.dope.breaking.domain.hashtag.HashtagType;
import com.dope.breaking.domain.media.UploadType;
import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostType;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.DetailPostResponseDto;
import com.dope.breaking.dto.post.LocationDto;
import com.dope.breaking.dto.post.PostRequestDto;
import com.dope.breaking.dto.post.WriterDto;
import com.dope.breaking.exception.CustomInternalErrorException;
import com.dope.breaking.exception.NotValidRequestBodyException;
import com.dope.breaking.exception.post.NoSuchPostException;
import com.dope.breaking.exception.post.PurchasedPostException;
import com.dope.breaking.exception.user.NoPermissionException;
import com.dope.breaking.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;

    private final UserRepository userRepository;

    private final PostLikeRepository postLikeRepository;

    private final HashtagRepository hashtagRepository;
    private final HashtagService hashtagService;

    private final MediaRepository mediaRepository;

    private final BookmarkRepository bookmarkRepository;

    private final PurchaseRepository purchaseRepository;

    private final MediaService mediaService;

    private final CommentRepository commentRepository;


    @Transactional
    public Long create(String username, String contentData, List<MultipartFile> files) throws Exception {
        User user = userRepository.findByUsername(username).get();

        PostRequestDto postRequestDto = transferPostRequestToObject(contentData);

        validatePostRequest(postRequestDto);

        PostType postType = confirmPostType(postRequestDto.getPostType());
        Post post = new Post();
        Long postId = null;

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

            post.setUser(user);
            postId = postRepository.save(post).getId();
            if(postRequestDto.getHashtagList() != null) {
                hashtagService.saveHashtag(postRequestDto.getHashtagList(), postId, HashtagType.POST);
            }

        } catch (Exception e) {
            throw new CustomInternalErrorException("???????????? ????????? ??? ????????????.");
        }

        List<String> mediaURL = new LinkedList<>(); //????????? ???????????? ??????.
        if (files != null && files.size() != 0 &&files.get(0).getSize() != 0) {//???????????? ????????? ????????? ????????? ??????????????? ????????? 1??? ????????????, byte??? 0?????? ????????????. ????????? ????????? ????????? ??????????????? DB??? ??????????????? ???.
            mediaURL = mediaService.uploadMedias(files, UploadType.ORIGINAL_POST_MEDIA);
            mediaService.createMediaEntities(mediaURL, post); //??????.
            String thumbImgURL = mediaService.makeThumbnail(mediaURL.get(postRequestDto.getThumbnailIndex()));
            post.setThumbnailImgURL(thumbImgURL);
            log.info(mediaRepository.findAllByPostId(postId).toString());
        } else {
            post.setThumbnailImgURL(null); //default??? null => ?????? ????????? ??????.
        }
        return postId;
    }

    @Transactional
    public void modify(long postId, String username, PostRequestDto postRequest) throws Exception {
        if (!postRepository.findById(postId).isPresent()) {
            throw new NoSuchPostException();
        }

        if (!postRepository.existsByIdAndUserId(postId, userRepository.findByUsername(username).get().getId())) {
            throw new NoPermissionException();
        }
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String contentData = objectMapper.writeValueAsString(postRequest);

        PostRequestDto postRequestDto = transferPostRequestToObject(contentData);

        validatePostRequest(postRequestDto);

        PostType postType = confirmPostType(postRequestDto.getPostType());

        Post modifyPost = postRepository.getById(postId);
        try {
            Location location = Location.builder()
                    .region(postRequestDto.getLocationDto().getRegion())
                    .latitude(postRequestDto.getLocationDto().getLatitude())
                    .longitude(postRequestDto.getLocationDto().getLongitude()).build();

            modifyPost.UpdatePost(postRequestDto.getTitle(), postRequestDto.getContent(), postType, location, postRequestDto.getPrice(), postRequestDto.getIsAnonymous(), postRequestDto.getEventTime());
            hashtagService.updateHashtag(postRequestDto.getHashtagList(), modifyPost.getId(), HashtagType.POST);

        } catch (Exception e) {
            log.info("????????? ?????? ??????");
            e.printStackTrace();
            throw new CustomInternalErrorException("???????????? ????????? ??? ????????????.");
        }
        log.info(modifyPost.toString());
    }


    @Transactional
    public DetailPostResponseDto read(Long postId, String crntUsername) {
        //1. ????????? ????????????.
        if (!postRepository.findById(postId).isPresent()) {
            throw new NoSuchPostException();
        }

        Post post = postRepository.getById(postId);

        //2. ?????? ????????? ????????? ????????? ????????? ??????
        boolean isLiked = false;
        boolean isBookmarked = false;
        boolean isPurchased = false;
        if (crntUsername != null) {
            User user = userRepository.findByUsername(crntUsername).get();
            isLiked = postLikeRepository.existsPostLikesByUserAndPost(user, post);
            isBookmarked = bookmarkRepository.existsByUserAndPost(user, post);
            isPurchased = purchaseRepository.existsByPostAndUser(post, user);
        }


        //????????? ??????.
        post.updateViewCount();

        WriterDto writerDto = WriterDto.builder()
                .nickname(post.getUser().getNickname())
                .phoneNumber(post.getUser().getPhoneNumber())
                .profileImgURL(post.getUser().getOriginalProfileImgURL())
                .userId(post.getUser().getId()).build();

        LocationDto locationDto = LocationDto.builder()
                .region(post.getLocation().getRegion())
                .latitude(post.getLocation().getLatitude())
                .longitude(post.getLocation().getLongitude()).build();

        DetailPostResponseDto detailPostResponseDto = DetailPostResponseDto.builder()
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .user(writerDto)
                .title(post.getTitle())
                .content(post.getContent())
                .mediaList(mediaRepository.findAllByPostId(postId).stream().map(media -> media.getMediaURL()).collect(Collectors.toList()))
                .hashtagList(post.getHashtags().stream().map(postHashtag -> postHashtag.getContent()).collect(Collectors.toList()))
                .location(locationDto)
                .price(post.getPrice())
                .postType(post.getPostType().getTitle())
                .isAnonymous(post.isAnonymous())
                .eventTime(post.getEventTime())
                .createdTime(post.getCreatedDate())
                .modifiedTime(post.getModifiedDate())
                .viewCount(post.getViewCount())
                .bookmarkedCount(bookmarkRepository.countByPost(post))
                .likeCount(postLikeRepository.countPostLikesByPost(post))
                .isSold(post.isSold())
                .soldCount(purchaseRepository.countByPost(post))
                .isHidden(post.isHidden())
                .totalCommentCount(commentRepository.countByPost(post))
                .build();

        return detailPostResponseDto;

    }


    @Transactional
    public ResponseEntity delete(long postId, String username){
        if (!postRepository.findById(postId).isPresent()) {
            throw new NoSuchPostException();
        }

        if (!postRepository.existsByIdAndUserId(postId, userRepository.findByUsername(username).get().getId())) {
            throw new NoPermissionException();
        }


        Post post = postRepository.getById(postId);
        if(purchaseRepository.existsByPost(post)){
            throw new PurchasedPostException();
        }

        List<String> preMediaURLs = post.getMediaList().stream().map(postEntity -> postEntity.getMediaURL()).collect(Collectors.toList());
        mediaService.deleteMedias(preMediaURLs);
        mediaService.deleteThumbnailImg(post.getThumbnailImgURL());

        postRepository.delete(post);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    private PostRequestDto transferPostRequestToObject(String contentData) {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PostRequestDto postRequestDto = new PostRequestDto();
        try {
            postRequestDto = mapper.readerFor(PostRequestDto.class).readValue(contentData);
        } catch (JsonMappingException e) {
            throw new CustomInternalErrorException(e.getMessage());
        } catch (JsonProcessingException e) {
            throw new CustomInternalErrorException(e.getMessage());
        }
        return postRequestDto;
    }

    private void validatePostRequest(PostRequestDto postRequestDto) {
        log.info(postRequestDto.toString());
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<PostRequestDto>> violations = validator.validate(postRequestDto);
        if (violations.size() > 0) {
            StringBuffer result = new StringBuffer();
            for (ConstraintViolation<PostRequestDto> violation : violations) {
                result.append(String.valueOf(violation.getPropertyPath())).append(", ");
            }
            result.delete(result.length() - 2, result.length());
            log.info(result.toString());

            throw new NotValidRequestBodyException(result.toString());
        }
    }


    private PostType confirmPostType(String requestPostType) {
        PostType postType = null;
        if (PostType.EXCLUSIVE.getTitle().equals(requestPostType)) {
            postType = PostType.EXCLUSIVE;
        } else if (PostType.CHARGED.getTitle().equals(requestPostType)) {
            postType = PostType.CHARGED;
        } else if (PostType.FREE.getTitle().equals(requestPostType)) {
            postType = PostType.FREE;
        }
        return postType;
    }


}
