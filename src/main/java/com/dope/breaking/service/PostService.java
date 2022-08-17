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
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.post.AlreadyNotPurchasableException;
import com.dope.breaking.exception.post.AlreadyPurchasableException;
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
        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);

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
                            .address(postRequestDto.getLocationDto().getAddress())
                            .latitude(postRequestDto.getLocationDto().getLatitude())
                            .longitude(postRequestDto.getLocationDto().getLongitude())
                            .region_1depth_name(postRequestDto.getLocationDto().getRegion_1depth_name())
                            .region_2depth_name(postRequestDto.getLocationDto().getRegion_2depth_name())
                            .build())
                    .eventDate(postRequestDto.getEventDate())
                    .isAnonymous(postRequestDto.getIsAnonymous())
                    .price(postRequestDto.getPrice())
                    .build();

            post.setUser(user);
            postId = postRepository.save(post).getId();
            if(postRequestDto.getHashtagList() != null) {
                hashtagService.saveHashtag(postRequestDto.getHashtagList(), postId, HashtagType.POST);
            }

        } catch (Exception e) {
            throw new CustomInternalErrorException(e.getMessage());
        }

        List<String> mediaURL = new LinkedList<>(); //순서를 지정하기 위함.
        if (files != null && files.size() != 0 &&files.get(0).getSize() != 0) {//사용자가 파일을 보내지 않아도 기본적으로 갯수는 1로 반영되며, byte는 0으로 반환된다. 따라서 파일이 확실히 존재할때만 DB에 반영되도록 함.
            mediaURL = mediaService.uploadMedias(files, UploadType.ORIGINAL_POST_MEDIA);
            mediaService.createMediaEntities(mediaURL, post); //저장.
            String thumbImgURL = mediaService.makeThumbnail(mediaURL.get(postRequestDto.getThumbnailIndex()));
            post.setThumbnailImgURL(thumbImgURL);
            log.info(mediaRepository.findAllByPostId(postId).toString());
        } else {
            post.setThumbnailImgURL(null); //default는 null => 기본 썸네일 지정.
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
                    .address(postRequestDto.getLocationDto().getAddress())
                    .latitude(postRequestDto.getLocationDto().getLatitude())
                    .longitude(postRequestDto.getLocationDto().getLongitude())
                    .region_1depth_name(postRequestDto.getLocationDto().getRegion_1depth_name())
                    .region_2depth_name(postRequestDto.getLocationDto().getRegion_2depth_name())
                    .build();

            modifyPost.UpdatePost(postRequestDto.getTitle(), postRequestDto.getContent(), postType, location, postRequestDto.getPrice(), postRequestDto.getIsAnonymous(), postRequestDto.getEventDate());
            hashtagService.updateHashtag(postRequestDto.getHashtagList(), modifyPost.getId(), HashtagType.POST);

        } catch (Exception e) {
            log.info("게시글 수정 실패");
            e.printStackTrace();
            throw new CustomInternalErrorException(e.getMessage());
        }
        log.info(modifyPost.toString());
    }


    @Transactional
    public DetailPostResponseDto read(Long postId, String crntUsername) {

        //1. 없다면 예외반환.
        Post post = postRepository.findById(postId).orElseThrow(NoSuchPostException:: new);

        //2. 현재 사용자 게시글 좋아요 했는지 판별
        boolean isLiked = false;
        boolean isBookmarked = false;
        boolean isPurchased = false;
        boolean isMyPost = false;
        if (crntUsername != null) {
            User user = userRepository.findByUsername(crntUsername).get();
            isLiked = postLikeRepository.existsPostLikesByUserAndPost(user, post);
            isBookmarked = bookmarkRepository.existsByUserAndPost(user, post);
            isPurchased = purchaseRepository.existsByPostAndUser(post, user);
            isMyPost = user == post.getUser();
        }

        WriterDto writerDto = null;

        if(!(!isMyPost && post.isAnonymous())){
            writerDto = WriterDto.builder()
                    .nickname(post.getUser().getNickname())
                    .profileImgURL(post.getUser().getOriginalProfileImgURL())
                    .userId(post.getUser().getId()).build();
        }


        //조회수 증가.
        post.updateViewCount();

        LocationDto locationDto = LocationDto.builder()
                .address(post.getLocation().getAddress())
                .latitude(post.getLocation().getLatitude())
                .longitude(post.getLocation().getLongitude())
                .region_1depth_name(post.getLocation().getRegion_1depth_name())
                .region_2depth_name(post.getLocation().getRegion_2depth_name())
                .build();

        DetailPostResponseDto detailPostResponseDto = DetailPostResponseDto.builder()
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .isPurchased(isPurchased)
                .user(writerDto)
                .title(post.getTitle())
                .content(post.getContent())
                .mediaList(mediaRepository.findAllByPostId(postId).stream().map(media -> media.getMediaURL()).collect(Collectors.toList()))
                .hashtagList(post.getHashtags().stream().map(postHashtag -> postHashtag.getContent()).collect(Collectors.toList()))
                .location(locationDto)
                .price(post.getPrice())
                .postType(post.getPostType().getTitle())
                .isAnonymous(post.isAnonymous())
                .eventDate(post.getEventDate())
                .createdDate(post.getCreatedDate())
                .modifiedDate(post.getModifiedDate())
                .viewCount(post.getViewCount())
                .bookmarkedCount(bookmarkRepository.countByPost(post))
                .likeCount(postLikeRepository.countPostLikesByPost(post))
                .isSold(post.isSold())
                .soldCount(purchaseRepository.countByPost(post))
                .isHidden(post.isHidden())
                .commentCount(commentRepository.countByPost(post))
                .isMyPost(isMyPost)
                .isPurchasable(post.getIsPurchasable())
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

    @Transactional
    public void deactivatePurchase(String username, Long postId){

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Post post = postRepository.findById(postId).orElseThrow(NoSuchPostException::new);

        if(post.getUser() != user){
            throw new NoPermissionException();
        }

        if(!post.getIsPurchasable()){
            throw new AlreadyNotPurchasableException();
        }

        post.updateIsPurchasable(false);

    }

    @Transactional
    public void activatePurchase(String username, Long postId){

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Post post = postRepository.findById(postId).orElseThrow(NoSuchPostException::new);

        if(post.getUser() != user){
            throw new NoPermissionException();
        }

        if(post.getIsPurchasable()){
            throw new AlreadyPurchasableException();
        }

        post.updateIsPurchasable(true);

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
        if (PostType.EXCLUSIVE.getTitle().equalsIgnoreCase(requestPostType)) {
            postType = PostType.EXCLUSIVE;
        } else if (PostType.CHARGED.getTitle().equalsIgnoreCase(requestPostType)) {
            postType = PostType.CHARGED;
        } else if (PostType.FREE.getTitle().equalsIgnoreCase(requestPostType)) {
            postType = PostType.FREE;
        } else if (PostType.MISSION.getTitle().equalsIgnoreCase(requestPostType)) {
            postType = PostType.MISSION;
        }
        return postType;
    }


}
