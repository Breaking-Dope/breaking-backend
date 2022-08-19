package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.financial.NotEnoughBalanceException;
import com.dope.breaking.exception.post.NoSuchPostException;
import com.dope.breaking.exception.post.NotPurchasablePostException;
import com.dope.breaking.exception.post.SoldExclusivePostException;
import com.dope.breaking.exception.user.NoPermissionException;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.PurchaseRepository;
import com.dope.breaking.repository.TransactionRepository;
import com.dope.breaking.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class PurchaseServiceTest {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FollowService followService;

    @DisplayName("무료제보를 구매할 경우, 1. 제보가 정상적으로 구매 된다. 2. balance 가 변하지 않는다.")
    @Test
    void purchaseFreePost() throws Exception {

        //Given
        User buyer = new User();
        buyer.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer", Role.USER);
        buyer.updateBalance(1000);
        userRepository.save(buyer);

        User seller = new User();
        seller.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "seller", Role.USER);
        seller.updateBalance(2000);
        userRepository.save(seller);

        List<MultipartFile> multipartFiles = new LinkedList<>();

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 0," +
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

        Long postId = postService.create("seller", json, multipartFiles);

        //When
        purchaseService.purchasePost("buyer", postId);

        //Then
        Assertions.assertEquals(1, purchaseRepository.findAll().size());
        Assertions.assertEquals(2, transactionRepository.findAll().size());
        Assertions.assertEquals(1000, buyer.getBalance());
        Assertions.assertEquals(2000, seller.getBalance());
        Assertions.assertTrue(postRepository.getById(postId).isSold());

    }

    @DisplayName("구매자의 잔액이 제보 판매금액보다 클 경우, 1. 구매가 정상적으로 진행된다, 2. 구매자와 판매자의 잔액이 업데이트 된다. 3. Transaction 객체가 두 개 생성된다.")
    @Test
    void purchaseChargedPost() throws Exception {

        //Given
        User buyer = new User();
        buyer.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer", Role.USER);
        buyer.updateBalance(3000);
        userRepository.save(buyer);

        User seller = new User();
        seller.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "seller", Role.USER);
        seller.updateBalance(4000);
        userRepository.save(seller);

        List<MultipartFile> multipartFiles = new LinkedList<>();

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 1000," +
                "\"isAnonymous\" : \"false\"," +
                "\"postType\" : \"charged\"," +
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

        Long postId = postService.create("seller", json, multipartFiles);

        //When
        purchaseService.purchasePost("buyer", postId);

        //Then
        Assertions.assertEquals(1, purchaseRepository.findAll().size());
        Assertions.assertEquals(2, transactionRepository.findAll().size());
        Assertions.assertEquals(2000, buyer.getBalance());
        Assertions.assertEquals(5000, seller.getBalance());

    }


    @DisplayName("구매자의 잔액이 제보 판매금액보다 작을 경우, 에러가 발생한다")
    @Test
    void purchasePostWhenPriceLargerThanBalance() throws Exception {

        //Given
        User buyer = new User();
        buyer.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer", Role.USER);
        buyer.updateBalance(999);
        userRepository.save(buyer);

        User seller = new User();
        seller.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "seller", Role.USER);
        seller.updateBalance(0);
        userRepository.save(seller);

        List<MultipartFile> multipartFiles = new LinkedList<>();

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 1000," +
                "\"isAnonymous\" : \"false\"," +
                "\"postType\" : \"charged\"," +
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

        Long postId = postService.create("seller", json, multipartFiles);


        //Then
        Assertions.assertThrows(NotEnoughBalanceException.class, ()
                -> purchaseService.purchasePost("buyer", postId)); //When

        //Then
        Assertions.assertEquals(0, purchaseRepository.findAll().size());
        Assertions.assertEquals(0, transactionRepository.findAll().size());
        Assertions.assertEquals(999, buyer.getBalance());
        Assertions.assertEquals(0, seller.getBalance());


    }

    @DisplayName("판매되지 않은 단독제보를 구매할 경우, 정상적으로 구매된다.")
    @Test
    void purchaseExclusivePost() throws Exception {

        //Given
        User buyer = new User();
        buyer.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer", Role.USER);
        buyer.updateBalance(2000);
        userRepository.save(buyer);

        User seller = new User();
        seller.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "seller", Role.USER);
        seller.updateBalance(0);
        userRepository.save(seller);

        List<MultipartFile> multipartFiles = new LinkedList<>();

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 1000," +
                "\"isAnonymous\" : \"false\"," +
                "\"postType\" : \"exclusive\"," +
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

        Long postId = postService.create("seller", json, multipartFiles);

        //When
        purchaseService.purchasePost("buyer", postId);

        //Then
        Assertions.assertEquals(1, purchaseRepository.findAll().size());
        Assertions.assertEquals(2, transactionRepository.findAll().size());
        Assertions.assertEquals(1000, buyer.getBalance());
        Assertions.assertEquals(1000, seller.getBalance());
        Assertions.assertTrue(postRepository.getById(postId).isSold());

    }

    @DisplayName("판매된 단독제보를 구매할 경우, 예외가 발생한다.")
    @Test
    void purchaseSoldExclusivePost() throws Exception {

        //Given
        User buyer1 = new User();
        buyer1.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer1", Role.USER);
        buyer1.updateBalance(2000);
        userRepository.save(buyer1);

        User buyer2 = new User();
        buyer2.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer2", Role.USER);
        buyer2.updateBalance(2000);
        userRepository.save(buyer2);

        User seller = new User();
        seller.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "seller", Role.USER);
        seller.updateBalance(0);
        userRepository.save(seller);

        List<MultipartFile> multipartFiles = new LinkedList<>();

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 1000," +
                "\"isAnonymous\" : \"false\"," +
                "\"postType\" : \"exclusive\"," +
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

        Long postId = postService.create("seller", json, multipartFiles);

        purchaseService.purchasePost("buyer1",postId);

        //Then
        Assertions.assertThrows(SoldExclusivePostException.class, ()
                -> purchaseService.purchasePost("buyer2", postId)); //When

        //Then
        Assertions.assertEquals(1, purchaseRepository.findAll().size());
        Assertions.assertEquals(2, transactionRepository.findAll().size());
        Assertions.assertEquals(1000, buyer1.getBalance());
        Assertions.assertEquals(2000, buyer2.getBalance());
        Assertions.assertEquals(1000, seller.getBalance());


    }

    @DisplayName("구매 비활성화 된 제보를 구매할 경우, 예외가 발생한다.")
    @Test
    void purchaseNotPurchasablePost() throws Exception {

        //Given
        User buyer = new User();
        buyer.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer", Role.USER);
        buyer.updateBalance(2000);
        userRepository.save(buyer);

        User seller = new User();
        seller.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "seller", Role.USER);
        seller.updateBalance(0);
        userRepository.save(seller);

        List<MultipartFile> multipartFiles = new LinkedList<>();

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 1000," +
                "\"isAnonymous\" : \"false\"," +
                "\"postType\" : \"exclusive\"," +
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

        Long postId = postService.create("seller", json, multipartFiles);

        Post post = postRepository.getById(postId);
        post.updateIsPurchasable(false);

        //Then
        Assertions.assertThrows(NotPurchasablePostException.class, ()
                -> purchaseService.purchasePost("buyer", postId)); //When

    }

    @DisplayName("존재하지 않는 유저일 경우, 예외가 발생한다.")
    @Test
    void purchaseWithNotExistingUsername() throws Exception {

        //Given
        User seller = new User();
        seller.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "seller", Role.USER);
        seller.updateBalance(0);
        userRepository.save(seller);

        List<MultipartFile> multipartFiles = new LinkedList<>();

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 1000," +
                "\"isAnonymous\" : \"false\"," +
                "\"postType\" : \"exclusive\"," +
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

        Long postId = postService.create("seller", json, multipartFiles);

        //Then
        Assertions.assertThrows(InvalidAccessTokenException.class, ()
                -> purchaseService.purchasePost("weirdUsername", postId)); //When

    }

    @DisplayName("존재하지 않는 제보를 구매할 경우, 예외가 발생한다.")
    @Test
    void purchaseNotExistingPost() {

        //Given
        User buyer = new User();
        buyer.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer", Role.USER);
        buyer.updateBalance(2000);
        userRepository.save(buyer);

        //Then
        Assertions.assertThrows(NoSuchPostException.class, ()
                -> purchaseService.purchasePost("buyer", 100L)); //When

    }

    @DisplayName("유저네임이 일치할 경우, 구매자 리스트가 정확히 반환된다")
    @Test
    void purchaseListWhenValidUsername() throws Exception {

        //Given
        User buyer1 = new User();
        buyer1.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer1", Role.USER);
        buyer1.updateBalance(1000);
        userRepository.save(buyer1);

        User buyer2 = new User();
        buyer2.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer2", Role.USER);
        buyer2.updateBalance(2000);
        userRepository.save(buyer2);

        User seller = new User();
        seller.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "seller", Role.USER);
        seller.updateBalance(2000);
        userRepository.save(seller);

        List<MultipartFile> multipartFiles = new LinkedList<>();

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 0," +
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

        Long postId = postService.create("seller", json, multipartFiles);

        purchaseService.purchasePost("buyer1", postId);
        purchaseService.purchasePost("buyer2", postId);

        //When
        followService.follow("seller", buyer1.getId());

        //Then
        assertTrue(purchaseService.purchaseList("seller",postId,null,10).get(0).isFollowing());
        assertFalse(purchaseService.purchaseList("seller",postId, null, 10).get(1).isFollowing());

    }

    @DisplayName("유저네임이 판매자의 유저네임이 아닐 경우, 예외가 발생한다")
    @Test
    void purchaseListWhenNotSellerUsername() throws Exception {

        //Given
        User buyer1 = new User();
        buyer1.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer1", Role.USER);
        buyer1.updateBalance(1000);
        userRepository.save(buyer1);

        User buyer2 = new User();
        buyer2.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer2", Role.USER);
        buyer2.updateBalance(2000);
        userRepository.save(buyer2);

        User seller = new User();
        seller.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "seller", Role.USER);
        seller.updateBalance(2000);
        userRepository.save(seller);

        List<MultipartFile> multipartFiles = new LinkedList<>();

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 0," +
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

        Long postId = postService.create("seller", json, multipartFiles);

        purchaseService.purchasePost("buyer1", postId);
        purchaseService.purchasePost("buyer2", postId);

        Assertions.assertThrows(NoPermissionException.class, ()
                ->  purchaseService.purchaseList("buyer1",postId, null, 20)); //When

    }

    @DisplayName("cursorId가 무효할 경우, 예외가 발생한다")
    @Test
    void purchaseListWithWrongCursorId() throws Exception {

        //Given
        User buyer1 = new User();
        buyer1.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer1", Role.USER);
        buyer1.updateBalance(1000);
        userRepository.save(buyer1);

        User buyer2 = new User();
        buyer2.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer2", Role.USER);
        buyer2.updateBalance(2000);
        userRepository.save(buyer2);

        User seller = new User();
        seller.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "seller", Role.USER);
        seller.updateBalance(2000);
        userRepository.save(seller);

        List<MultipartFile> multipartFiles = new LinkedList<>();

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 0," +
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

        Long postId = postService.create("seller", json, multipartFiles);

        purchaseService.purchasePost("buyer1", postId);
        purchaseService.purchasePost("buyer2", postId);

        Assertions.assertThrows(NoSuchPostException.class, ()
                ->  purchaseService.purchaseList("seller", postId, 100L, 10)); //When

    }

}