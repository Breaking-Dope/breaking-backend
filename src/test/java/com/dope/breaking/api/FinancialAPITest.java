package com.dope.breaking.api;

import com.dope.breaking.domain.financial.TransactionType;
import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.financial.AmountRequestDto;
import com.dope.breaking.repository.*;
import com.dope.breaking.service.FollowService;
import com.dope.breaking.service.PostService;
import com.dope.breaking.service.PurchaseService;
import com.dope.breaking.service.StatementService;
import com.dope.breaking.withMockCustomAuthorize.WithMockCustomUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class FinancialAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private StatementService statementService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private FollowService followService;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;


    @BeforeEach
    public void createUserInfo() {

        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.PRESS)
                .build();

        userRepository.save(user);

    }

    @DisplayName("유저네임이 일치할시, 입금이 정상적으로 진행된다.")
    @Test
    @WithMockCustomUser
    @Transactional
    void deposit() throws Exception {

        //Given
        AmountRequestDto depositAmount = new AmountRequestDto(20000);
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);

        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(depositAmount);

        //When
        this.mockMvc.perform(post("/financial/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk()); //Then

        //Then
        Assertions.assertEquals(1, statementRepository.findAll().size());
        Assertions.assertEquals(1, transactionRepository.findAll().size());
        Assertions.assertEquals(20000, userRepository.findByUsername("12345g").get().getBalance());

    }

    @DisplayName("잔액이 출금액보다 많을 시, 출금이 정상적으로 진행된다.")
    @Test
    @WithMockCustomUser
    @Transactional
    void withdraw() throws Exception{

        //Given
        statementService.depositOrWithdraw("12345g",30000, TransactionType.DEPOSIT);

        AmountRequestDto withdrawAmount = new AmountRequestDto(10000);
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);

        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(withdrawAmount);

        //When
        this.mockMvc.perform(post("/financial/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk()); //Then

        //Then
        Assertions.assertEquals(2, statementRepository.findAll().size());
        Assertions.assertEquals(2, transactionRepository.findAll().size());
        Assertions.assertEquals(20000, userRepository.findByUsername("12345g").get().getBalance());

    }

    @DisplayName("잔액이 출금액과 동일할 시, 출금이 정상적으로 진행된다.")
    @Test
    @WithMockCustomUser
    @Transactional
    void withdrawAll() throws Exception{

        //Given
        statementService.depositOrWithdraw("12345g",30000, TransactionType.DEPOSIT);

        AmountRequestDto withdrawAmount = new AmountRequestDto(30000);
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);

        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(withdrawAmount);

        //When
        this.mockMvc.perform(post("/financial/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk()); //Then

        //Then
        Assertions.assertEquals(2, statementRepository.findAll().size());
        Assertions.assertEquals(2, transactionRepository.findAll().size());
        Assertions.assertEquals(0, userRepository.findByUsername("12345g").get().getBalance());

    }

    @DisplayName("잔액이 출금액보다 적을시, 예외가 발생한다.")
    @Test
    @WithMockCustomUser
    @Transactional
    void withdrawMoreThanBalance() throws Exception{

        //Given
        statementService.depositOrWithdraw("12345g",30000, TransactionType.DEPOSIT);

        AmountRequestDto withdrawAmount = new AmountRequestDto(40000);
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);

        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(withdrawAmount);

        //When
        this.mockMvc.perform(post("/financial/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest()); //Then

    }

    @DisplayName("무료제보를 구매할 경우, 제보가 정상적으로 구매된다")
    @Test
    @WithMockCustomUser
    @Transactional
    void purchaseFreePost() throws Exception {

        //Given
        User buyer = new User();
        buyer.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer", Role.USER);
        buyer.updateBalance(2000);
        userRepository.save(buyer);

        User seller = new User();
        seller.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "seller", Role.USER);
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
        this.mockMvc.perform(post("/post/{postId}/purchase", postId))
                .andExpect(status().isOk()); //Then

    }

    @DisplayName("잔액이 제보 가격보다 적을 경우, 예외가 발생한다.")
    @Test
    @WithMockCustomUser
    @Transactional
    void purchaseWhenPriceBiggerThanBalance() throws Exception {

        //Given
        User user = userRepository.findByUsername("12345g").get();
        user.updateBalance(2000);

        User seller = new User();
        seller.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "seller", Role.USER);
        seller.updateBalance(0);
        userRepository.save(seller);

        List<MultipartFile> multipartFiles = new LinkedList<>();

        String json = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 3000," +
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
        this.mockMvc.perform(post("/post/{postId}/purchase", postId))
                .andExpect(status().isBadRequest()); //Then

    }

    @DisplayName("이미 판매 된 단독제보을 구매할 경우, 예외가 발생한다.")
    @WithMockCustomUser
    @Transactional
    @Test
    void purchaseSoldExclusivePost() throws Exception {

        //Given
        User buyer1 = new User();
        buyer1.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer1", Role.USER);
        buyer1.updateBalance(2000);
        userRepository.save(buyer1);

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

        //When
        this.mockMvc.perform(post("/post/{postId}/purchase", postId))
                .andExpect(status().isBadRequest()); //Then

    }

    @DisplayName("브레이킹 미션에 제출 된 제보를 미션을 게시한 언론사가 게시할 경우, 제보가 정상적으로 구매된다.")
    @WithMockCustomUser
    @Transactional
    @Test
    void purchaseMissionPostByMissionOwner() throws Exception {

        //Given
        User user = userRepository.findByUsername("12345g").get();
        user.updateBalance(2000);

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
                "\"postType\" : \"mission\"," +
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
        Mission mission  = new Mission(user,"title","content",null,null,null);
        missionRepository.save(mission);

        postRepository.getById(postId).updateMission(mission);

        //When
        this.mockMvc.perform(post("/post/{postId}/purchase", postId))
                .andExpect(status().isOk()); //Then

        //Then
        Assertions.assertEquals(1,purchaseRepository.findAll().size());

    }

    @DisplayName("브레이킹 미션에 제출 된 제보를 미션을 게시한 언론사가 게시할 경우, 제보가 정상적으로 구매된다.")
    @WithMockCustomUser
    @Transactional
    @Test
    void purchaseMissionPostByOtherUser() throws Exception {

        //Given
        User user = userRepository.findByUsername("12345g").get();
        user.updateBalance(2000);

        User buyer1 = new User();
        buyer1.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "buyer1", Role.USER);
        buyer1.updateBalance(2000);
        userRepository.save(buyer1);

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
                "\"postType\" : \"mission\"," +
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
        Mission mission  = new Mission(buyer1,"title","content",null,null,null);
        missionRepository.save(mission);

        postRepository.getById(postId).updateMission(mission);

        //When
        this.mockMvc.perform(post("/post/{postId}/purchase", postId))
                .andExpect(status().isNotAcceptable()); //Then

    }

    @DisplayName("유저네임이 일치할 경우, 입출금 및 제보 거래내역이 최신순으로 반환된다.")
    @WithMockCustomUser
    @Transactional
    @Test
    void transactionList() throws Exception {

        //Given
        statementService.depositOrWithdraw("12345g", 30000, TransactionType.DEPOSIT);
        statementService.depositOrWithdraw("12345g", 10000, TransactionType.WITHDRAW);

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
        purchaseService.purchasePost("12345g",postId);

        Long postId2 = postService.create("12345g", json, multipartFiles);
        purchaseService.purchasePost("seller",postId2);

        //Then
        this.mockMvc.perform(get("/profile/transaction?cursor=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionType").value("sell_post"))
                .andExpect(jsonPath("$[1].transactionType").value("buy_post"))
                .andExpect(jsonPath("$[2].transactionType").value("withdraw"))
                .andExpect(jsonPath("$[3].transactionType").value("deposit"));

    }

}