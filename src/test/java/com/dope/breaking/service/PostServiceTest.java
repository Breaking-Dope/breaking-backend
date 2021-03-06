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

import javax.persistence.EntityManager;

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
    @Test
    void saveUser() {
        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS) // ?????? ????????? USER ??? ??????
                .build();
        userRepository.save(user);
    }

    @DisplayName(value = "?????? ????????? ?????? ????????? ???, ???????????? ????????????.")
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
                "\"eventTime\" : \"2020-01-01 14:01:01\"," +
                "\"location\" : {" +
                " \"region\" : \"abgujung\"," +
                "\"longitude\" : 12.1234," +
                "\"latitude\" : 12.12345" +
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


    @DisplayName(value = "?????? ????????? ?????? ????????? ???, ????????? ????????????.")
    @Test
    void createWithNullField() throws Exception {
        //Given

        List<MultipartFile> multipartFiles = new LinkedList<>();

        String json = "{" +
                "\"title\" : null," +
                "\"content\" : \"content\"," +
                "\"price\" : 123," +
                "\"isAnonymous\" : \"false\"," +
                "\"postType\" : \"free\"," +
                "\"eventTime\" : \"2020-01-01 14:01:01\"," +
                "\"location\" : {" +
                " \"region\" : \"abgujung\"," +
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



    @DisplayName(value = "???????????? ???????????? ????????? ???, ??????????????? ????????? ??????.")
    @Test
    void modify() throws Exception {
        //Given
        LocationDto location = LocationDto.builder()
                .longitude(1.2)
                .region("andong")
                .latitude(1.3).build();
        List<String> hashTags = new LinkedList<>();
        hashTags.add("tag2");


        PostRequestDto postRequestDto = PostRequestDto.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType("free")
                .eventTime(LocalDateTime.parse("2016-10-31 23:59:59",
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

    @DisplayName(value = "????????? ????????? ???????????????, ????????? ????????????.")
    @Test
    void modifyWithNullField() throws Exception {
        //Given
        LocationDto location = LocationDto.builder()
                .longitude(1.2)
                .region("andong")
                .latitude(1.3).build();
        List<String> hashTags = new LinkedList<>();
        hashTags.add("tag2");


        PostRequestDto postRequestDto = PostRequestDto.builder()
                .title(null)
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType("free")
                .eventTime(LocalDateTime.parse("2016-10-31 23:59:59",
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



    @DisplayName(value = "?????? ???????????? ???????????? ??? ???, ????????? ????????????.")
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

    @DisplayName(value = "??? ????????? ???????????? ???????????? ??? ???, ????????? ????????????.")
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





    @DisplayName(value = "???????????? ?????? ?????? ????????? ???????????? ??????????????? ??? ???, ???????????? ????????????.")
    @Test
    void readWithAnonymous() {
        Location location = Location.builder()
                .longitude(1.2)
                .region("andong")
                .latitude(1.3).build();


        Post post= Post.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType(PostType.FREE)
                .eventTime(LocalDateTime.parse("2016-10-31 23:59:59",
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
    }


    @DisplayName(value = "???????????? ???????????? ????????? ?????? ???, ???????????? ????????????.")
    @Test
    void readWithLikedUser() {
        Location location = Location.builder()
                .longitude(1.2)
                .region("andong")
                .latitude(1.3).build();


        Post post= Post.builder()
                .title("title")
                .content("content")
                .price(123)
                .isAnonymous(false)
                .postType(PostType.FREE)
                .eventTime(LocalDateTime.parse("2016-10-31 23:59:59",
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
    }

    @DisplayName("???????????? ?????? ???????????? ????????? ???, ????????? ????????????.")
    @Test
    void deleteNoSuchPost() {

        //Given

        //then
        Assertions.assertThrows(NoSuchPostException.class, () -> postService.delete(-1, "12345g")); //When

    }

    @DisplayName("??? ????????? ????????? ???????????? ????????? ???, ????????? ????????????.")
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

    @DisplayName("?????? ????????? ??? ???????????? ????????? ???, ????????? ????????????.")
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


    @DisplayName("???????????? ???????????? ????????? ???, ???????????? ????????????.")
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
}