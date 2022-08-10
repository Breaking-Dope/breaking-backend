package com.dope.breaking.service;

import com.dope.breaking.domain.financial.Purchase;
import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostType;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.DetailPostResponseDto;
import com.dope.breaking.dto.post.LocationDto;
import com.dope.breaking.dto.post.PostRequestDto;
import com.dope.breaking.exception.NotValidRequestBodyException;
import com.dope.breaking.exception.post.AlreadyNotPurchasableException;
import com.dope.breaking.exception.post.AlreadyPurchasableException;
import com.dope.breaking.exception.post.NoSuchPostException;
import com.dope.breaking.exception.post.PurchasedPostException;
import com.dope.breaking.exception.user.NoPermissionException;
import com.dope.breaking.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class PostServiceTest {

    @Autowired
    PostService postService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PurchaseRepository purchaseRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PasswordEncoder passwordEncoder;


    @BeforeEach
    void saveUser() {
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // 최초 가입시 USER 로 설정
                .build();
        userRepository.save(user);
    }

    @DisplayName(value = "요청 내용이 모두 충족될 시, 게시글은 저장된다.")
    @Test
    void create() throws Exception {
        //Given
        List<MultipartFile> multipartFiles = new LinkedList<>();

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 123," +
                "\"isAnonymous\" : \"false\"," +
                "\"postType\" : \"free\"," +
                "\"eventDate\" : \"2020-01-01 14:01:01\"," +
                "\"location\" : {" +
                " \"address\" : \"address\"," +
                "\"longitude\" : 12.1234," +
                "\"latitude\" : 12.12345," +
                "\"region_1depth_name\" : \"region_1depth_name\"," +
                "\"region_2depth_name\" : \"region_2depth_name\" " +
                "}," +
                "\"hashtagList\" : [" +
                "\"hello\", \"hello2\"]," +
                "\"thumbnailIndex\" : 0" +
                "}";

        //When
        long postId = postService.create("12345g", json , multipartFiles);

        //Then
        Assertions.assertTrue(postRepository.existsById(postId));
        Assertions.assertEquals("12345g", postRepository.getById(postId).getUser().getUsername());
    }


    @DisplayName(value = "요청 내용이 일부 누락될 시, 예외가 반환된다.")
    @Test
    void createWithNullField() throws Exception {
        //Given

        List<MultipartFile> multipartFiles = new LinkedList<>();

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 123," +
                "\"isAnonymous\" : \"false\"," +
                "\"postType\" : \"free\"," +
                "\"eventDate\" : \"2020-01-01 14:01:01\"," +
                "\"location\" : {" +
                " \"address\" : \"address\"," +
                "\"longitude\" : 12.1234," +
                "\"latitude\" : 12.12345" +
                "}," +
                "\"hashtagList\" : [" +
                "\"hello\", \"hello2\"]," +
                "\"thumbnailIndex\" : 0" +
                "}";


        //Then
        Assertions.assertThrows(NotValidRequestBodyException.class, () -> postService.create("12345g", json , multipartFiles)); //When
    }



    @DisplayName(value = "작성자가 게시글을 수정할 시, 정상적으로 수정이 된다.")
    @Test
    void modify() throws Exception {
        //Given
        LocationDto location = LocationDto.builder()
                .longitude(1.2)
                .address("andong")
                .latitude(1.3)
                .region_1depth_name("region1")
                .region_2depth_name("region2")
        .build();
        List<String> hashTags = new LinkedList<>();
        hashTags.add("tag2");


        PostRequestDto postRequestDto = PostRequestDto.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType("free")
                .eventDate(LocalDateTime.parse("2016-10-31 23:59:59",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .locationDto(location)
                .hashtagList(hashTags).build();

        Post post = new Post();
        Long postId = postRepository.save(post).getId();
        User user = userRepository.findByUsername("12345g").get();
        post.setUser(user);

        //When
        postService.modify(postId, "12345g", postRequestDto);

        //Then
        Assertions.assertEquals("12345g", post.getUser().getUsername());
        Assertions.assertEquals("title", post.getTitle());
        Assertions.assertEquals("content", post.getContent());
        Assertions.assertEquals(123, post.getPrice());
    }

    @DisplayName(value = "누락된 필드가 존재한다면, 예외가 반환된다.")
    @Test
    void modifyWithNullField() throws Exception {
        //Given
        LocationDto location = LocationDto.builder()
                .longitude(1.2)
                .address("andong")
                .latitude(1.3)
                .region_1depth_name("region1")
                .region_2depth_name("region2")
                .build();

        List<String> hashTags = new LinkedList<>();
        hashTags.add("tag2");


        PostRequestDto postRequestDto = PostRequestDto.builder()
                .title(null)
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType("free")
                .eventDate(LocalDateTime.parse("2016-10-31 23:59:59",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .locationDto(location)
                .hashtagList(hashTags).build();

        Post post = new Post();
        Long postId = postRepository.save(post).getId();
        User user = userRepository.findByUsername("12345g").get();
        post.setUser(user);

        //Then
        Assertions.assertThrows(NotValidRequestBodyException.class, () -> postService.modify(postId, "12345g", postRequestDto));

    }



    @DisplayName(value = "없는 게시글을 수정하려 할 시, 예외가 반환된다.")
    @Test
    void modifyNoSuchPost() throws Exception {
        //Given
        LocationDto location = LocationDto.builder().build();

        PostRequestDto postRequestDto = PostRequestDto.builder().build();

        Post post = new Post();
        Long postId = postRepository.save(post).getId();
        User user = userRepository.findByUsername("12345g").get();
        post.setUser(user);

        //Then
        Assertions.assertThrows(NoSuchPostException.class, () -> postService.modify(-1, "12345g", postRequestDto));//When

    }

    @DisplayName(value = "타 유저의 게시글을 수정하려 할 시, 예외가 반환된다.")
    @Test
    void modifyNoPermission() throws Exception {
        //Given
        LocationDto location = LocationDto.builder().build();

        PostRequestDto postRequestDto = PostRequestDto.builder().build();

        Post post = new Post();
        Long postId = postRepository.save(post).getId();
        User user = userRepository.findByUsername("12345g").get();
        post.setUser(user);

        User user1  = User.builder()
                .username("123g").build();
        userRepository.save(user1);

        //Then
        Assertions.assertThrows(NoPermissionException.class, () -> postService.modify(postId, "123g", postRequestDto));//When
    }


    @DisplayName(value = "로그인을 하지 않은 유저가 게시글을 조회하려고 할 시, 게시글이 반환된다.")
    @Test
    void readWithAnonymous() {
        Location location = Location.builder()
                .longitude(1.2)
                .address("andong")
                .latitude(1.3)
                .region_1depth_name("region1")
                .region_2depth_name("region2")
                .build();


        Post post= Post.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType(PostType.FREE)
                .eventDate(LocalDateTime.parse("2016-10-31 23:59:59",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .location(location)
                .build();


        long postId = postRepository.save(post).getId();
        User user = userRepository.findByUsername("12345g").get();
        post.setUser(user);

        //When
        DetailPostResponseDto detailPostResponseDto = postService.read(postId, null);

        //Then
        Assertions.assertFalse(detailPostResponseDto.isBookmarked());
        Assertions.assertFalse(detailPostResponseDto.isLiked());
        Assertions.assertFalse(detailPostResponseDto.isPurchased());
        Assertions.assertFalse(detailPostResponseDto.isMyPost());
    }

    @DisplayName(value = "로그인을 하지 않은 유저가 익명 게시글을 조회하려고 할 시, 제보자 정보가 null로 반환된다.")
    @Test
    void readAnonymousPostWithoutLogin() {
        Location location = Location.builder()
                .longitude(1.2)
                .address("andong")
                .latitude(1.3)
                .region_1depth_name("region1")
                .region_2depth_name("region2")
                .build();


        Post post= Post.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(true)
                .postType(PostType.FREE)
                .eventDate(LocalDateTime.parse("2016-10-31 23:59:59",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .location(location)
                .build();


        long postId = postRepository.save(post).getId();
        User user = userRepository.findByUsername("12345g").get();
        post.setUser(user);

        //When
        DetailPostResponseDto detailPostResponseDto = postService.read(postId, null);

        //Then
        Assertions.assertFalse(detailPostResponseDto.isMyPost());
        Assertions.assertNull(detailPostResponseDto.getUser());

    }


    @DisplayName(value = "로그인한 사용자가 게시글 조회 시, 게시글이 반환한다.")
    @Test
    void readByLoggedinUser() {
        Location location = Location.builder()
                .longitude(1.2)
                .address("andong")
                .latitude(1.3)
                .region_1depth_name("region1")
                .region_2depth_name("region2")
                .build();


        Post post= Post.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType(PostType.FREE)
                .eventDate(LocalDateTime.parse("2016-10-31 23:59:59",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .location(location)
                .build();


        long postId = postRepository.save(post).getId();
        User user = userRepository.findByUsername("12345g").get();
        post.setUser(user);

        //When
        DetailPostResponseDto detailPostResponseDto = postService.read(postId, "12345g");

        //Then
        Assertions.assertFalse(detailPostResponseDto.isBookmarked());
        Assertions.assertFalse(detailPostResponseDto.isLiked());
        Assertions.assertFalse(detailPostResponseDto.isPurchased());
        Assertions.assertTrue(detailPostResponseDto.isMyPost());
    }

    @DisplayName(value = "다른 사용자가 익명 게시글을 조회할 시, 사용자 정보는 null로 반환된다.")
    @Test
    void readAnonymousByOtherUser() {
        Location location = Location.builder()
                .longitude(1.2)
                .address("andong")
                .latitude(1.3)
                .region_1depth_name("region1")
                .region_2depth_name("region2")
                .build();


        Post post= Post.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(true)
                .postType(PostType.FREE)
                .eventDate(LocalDateTime.parse("2016-10-31 23:59:59",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .location(location)
                .build();


        long postId = postRepository.save(post).getId();
        User user = new User();
        userRepository.save(user);
        post.setUser(user);

        //When
        DetailPostResponseDto detailPostResponseDto = postService.read(postId, "12345g");

        //Then
       Assertions.assertNull(detailPostResponseDto.getUser());

    }

    @DisplayName(value = "본인이 본인의 익명 게시글을 조회할 시, 사용자 정보가 나타난다 반환된다.")
    @Test
    void readAnonymousByWriter() {
        Location location = Location.builder()
                .longitude(1.2)
                .address("andong")
                .latitude(1.3)
                .region_1depth_name("region1")
                .region_2depth_name("region2")
                .build();


        Post post= Post.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(true)
                .postType(PostType.FREE)
                .eventDate(LocalDateTime.parse("2016-10-31 23:59:59",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .location(location)
                .build();


        long postId = postRepository.save(post).getId();
        User user = userRepository.findByUsername("12345g").get();
        post.setUser(user);

        //When
        DetailPostResponseDto detailPostResponseDto = postService.read(postId, "12345g");

        //Then
        Assertions.assertNotNull(detailPostResponseDto.getUser().getUserId());
        Assertions.assertTrue(detailPostResponseDto.isMyPost());

    }

    @DisplayName("존재하지 않는 게시글을 삭제할 시, 예외가 반환된다.")
    @Test
    void deleteNoSuchPost() {

        //Given

        //then
        Assertions.assertThrows(NoSuchPostException.class, () -> postService.delete(-1, "12345g")); //When

    }

    @DisplayName("타 유저가 작성한 게시글을 삭제할 시, 예외가 반환된다.")
    @Test
    void deleteNoPermission() {

        //Given
        Post post = new Post();
        long postId = postRepository.save(post).getId();
        User user = User.builder()
                .username("123").build();
        userRepository.save(user);
        post.setUser(user);

        //then
        Assertions.assertThrows(NoPermissionException.class, () -> postService.delete(postId, "12345g")); //When

    }

    @DisplayName("이미 구매가 된 게시글을 삭제할 시, 예외가 반환된다.")
    @Test
    void deletePurchasedPost() {

        //Given
        Post post = new Post();
        long postId = postRepository.save(post).getId();
        User user = userRepository.findByUsername("12345g").get();

        post.setUser(user);

        User buyer = User.builder().username("123").build();

        userRepository.save(buyer);
        Purchase purchase = Purchase.builder()
                .post(post)
                .user(buyer).build();

        purchaseRepository.save(purchase);


        //then
        Assertions.assertThrows(PurchasedPostException.class, () -> postService.delete(postId, "12345g")); //When

    }


    @DisplayName("작성자가 게시글을 삭제할 시, 게시글이 삭제된다.")
    @Test
    void delete() {

        //Given
        Post post = new Post();
        long postId = postRepository.save(post).getId();
        User user = userRepository.findByUsername("12345g").get();
        post.setUser(user);


        //When
        postService.delete(postId, "12345g");

        //then
        Assertions.assertFalse(postRepository.existsById(postId));

    }

    @DisplayName("구매 비활성화 된 제보를 활성화 할 경우, 제보가 활성화 된다.")
    @Test
    void activatePurchasedDeactivatedPost(){

        //Given
        User user = userRepository.findByUsername("12345g").get();

        Post post = new Post();
        post.setUser(user);
        post.updateIsPurchasable(false);
        long postId = postRepository.save(post).getId();

        //When
        postService.activatePurchase("12345g", post.getId());

        //then
        Assertions.assertTrue(post.getIsPurchasable());

    }

    @DisplayName("구매 활성화 된 제보를 활성화 할 경우, 예외가 발생한다.")
    @Test
    void activatePurchaseActivatedPost(){

        //Given
        User user = userRepository.findByUsername("12345g").get();

        Post post = new Post();
        post.setUser(user);
        long postId = postRepository.save(post).getId();

        //Then
        Assertions.assertThrows(AlreadyPurchasableException.class, ()
                -> postService.activatePurchase("12345g", postId)); //When

    }

    @DisplayName("사용자가 다른 이의 제보를 활성화 할 경우, 에외가 발생한다.")
    @Test
    void activatePurchaseByAnotherUser(){

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);
        userRepository.save(user);


        Post post = new Post();
        post.setUser(userRepository.findByUsername("12345g").get());
        post.updateIsPurchasable(false);
        long postId = postRepository.save(post).getId();

        //Then
        Assertions.assertThrows(NoPermissionException.class, ()
                -> postService.activatePurchase("username", postId)); //When


    }

    @DisplayName("구매 활성화 된 제보를 비활성화 할 경우, 제보가 비활성화 된다.")
    @Test
    void deactivateActivatedPost(){

        //Given
        User user = userRepository.findByUsername("12345g").get();
        Post post = new Post();
        post.setUser(user);
        long postId = postRepository.save(post).getId();

        //When
        postService.deactivatePurchase("12345g", post.getId());

        //then
        Assertions.assertFalse(post.getIsPurchasable());

    }

    @DisplayName("구매 비활성화 된 제보를 비활성화 할 경우, 예외가 발생한다.")
    @Test
    void deactivateDeactivatedPost(){

        //Given
        User user = userRepository.findByUsername("12345g").get();
        Post post = new Post();
        post.setUser(user);
        post.updateIsPurchasable(false);
        long postId = postRepository.save(post).getId();
        //Then
        Assertions.assertThrows(AlreadyNotPurchasableException.class, ()
                -> postService.deactivatePurchase("12345g", postId)); //When

    }

}