package com.dope.breaking.service;

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
import com.dope.breaking.exception.post.NoSuchPostException;
import com.dope.breaking.exception.user.NoPermissionException;
import com.dope.breaking.repository.MediaRepository;
import com.dope.breaking.repository.PostLikeRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


    private final MediaRepository mediaRepository;

    private final MediaService mediaService;


    @Transactional
    public Long create(String username, String contentData, List<MultipartFile> files) throws Exception {
        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);

        PostRequestDto postRequestDto = transferPostRequestToObject(contentData);

        validatePostRequest(postRequestDto);

        PostType postType = confirmPostType(postRequestDto.getPostType());
        Post post = new Post();
        Long postid = null;

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
            postid = postRepository.save(post).getId();

        } catch (Exception e) {
            throw new CustomInternalErrorException("게시글을 등록할 수 없습니다.");
        }

        List<String> mediaURL = new LinkedList<>(); //순서를 지정하기 위함.
        if (files != null && files.get(0).getSize() != 0) {//사용자가 파일을 보내지 않아도 기본적으로 갯수는 1로 반영되며, byte는 0으로 반환된다. 따라서 파일이 확실히 존재할때만 DB에 반영되도록 함.
            mediaURL = mediaService.uploadMedias(files, UploadType.ORIGINAL_POST_MEDIA);
            mediaService.createMediaEntities(mediaURL, post); //저장.
            String thumbImgURL = mediaService.makeThumbnail(mediaURL.get(postRequestDto.getThumbnailIndex()));
            post.setThumbnailImgURL(thumbImgURL);
        } else {
            post.setThumbnailImgURL(null); //default는 null => 기본 썸네일 지정.
        }
        return postid;
    }

    @Transactional
    public void modify(long postId, String username, String contentData, List<MultipartFile> files) throws Exception {
        if (!postRepository.existsByIdAndUserId(postId, userRepository.findByUsername(username).get().getId())) {
            throw new NoPermissionException();
        }

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
        } catch (Exception e) {
            log.info("게시글 수정 실패");
            throw new CustomInternalErrorException("게시글을 수정할 수 없습니다.");
        }

        List<String> preMediaURL = mediaService.preMediaURL(postId); //기존 URL
        List<String> mediaURL = new LinkedList<>();
        if (files != null && files.get(0).getSize() != 0) {
            mediaURL = mediaService.uploadMedias(files, UploadType.ORIGINAL_POST_MEDIA);
            mediaService.modifyMediaEntities(preMediaURL, mediaURL, postId);
            if(modifyPost.getThumbnailImgURL() != null){
                mediaService.deleteThumbnailImg(modifyPost.getThumbnailImgURL());
            }
            mediaService.deleteMedias(preMediaURL);
            String thumbImgURL = mediaService.makeThumbnail(mediaURL.get(postRequestDto.getThumbnailIndex()));
            modifyPost.setThumbnailImgURL(thumbImgURL);
        } else {
            mediaService.modifyMediaEntities(preMediaURL, mediaURL, postId);
            mediaService.deleteMedias(preMediaURL);
            if(modifyPost.getThumbnailImgURL() != null){
                mediaService.deleteThumbnailImg(modifyPost.getThumbnailImgURL());
            }
            modifyPost.setThumbnailImgURL(null);
        }
    }


    public DetailPostResponseDto read(Long postId, String crntUsername){
        //1. 없다면 예외반환.
        if(!postRepository.findById(postId).isPresent()){
            throw new NoSuchPostException();
        }

        //2. 현재 사용자 게시글 좋아요 했는지 판별
        boolean hasLiked = false;
        if(crntUsername != null) {
            hasLiked = postLikeRepository.existsPostLikesByUserAndPost(userRepository.findByUsername(crntUsername).get(), postRepository.findById(postId).get()) ? true : false;
        }

        //Post 가져오기
        Post post = postRepository.getById(postId);

        //조회수 증가.
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
                .hasLiked(hasLiked)
                .writerDto(writerDto)
                .title(post.getTitle())
                .content(post.getContent())
                .mediaList(mediaRepository.findAllByPostId(postId).stream().map(media -> media.getMediaURL()).collect(Collectors.toList()))
                .location(locationDto)
                .price(post.getPrice())
                .postType(post.getPostType().getTitle())
                .isAnonymous(post.isAnonymous())
                .eventTime(post.getEventTime())
                .createdDate(post.getCreatedDate())
                .modifiedDate(post.getModifiedDate())
                .viewCount(post.getViewCount())
                .shareCount(post.getBookmarkList().size())
                .isSold(post.isSold())
                .soldCount(post.getBuyerList().size())
                .isHidden(post.isHidden())
                .build();

        return detailPostResponseDto;

    }


    public boolean existByPostIdAndUserId(long postid, long userid) {
        return postRepository.existsByIdAndUserId(postid, userid);
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


    private PostType confirmPostType(String reqeustPostType) {
        PostType postType = null;
        if (PostType.EXCLUSIVE.getTitle().equals(reqeustPostType)) {
            postType = PostType.EXCLUSIVE;
        } else if (PostType.CHARGED.getTitle().equals(reqeustPostType)) {
            postType = PostType.CHARGED;
        } else if (PostType.FREE.getTitle().equals(reqeustPostType)) {
            postType = PostType.FREE;
        }
        return postType;
    }


}
