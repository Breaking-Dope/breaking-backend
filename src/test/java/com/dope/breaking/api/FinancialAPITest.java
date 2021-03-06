package com.dope.breaking.api;

import com.dope.breaking.domain.financial.TransactionType;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.financial.AmountRequestDto;
import com.dope.breaking.repository.StatementRepository;
import com.dope.breaking.repository.TransactionRepository;
import com.dope.breaking.repository.UserRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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


    @BeforeEach
    public void createUserInfo() {

        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.USER) // ?????? ????????? USER ??? ??????
                .build();

        userRepository.save(user);

    }

    @DisplayName("??????????????? ????????????, ????????? ??????????????? ????????????.")
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

    @DisplayName("????????? ??????????????? ?????? ???, ????????? ??????????????? ????????????.")
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

    @DisplayName("????????? ???????????? ????????? ???, ????????? ??????????????? ????????????.")
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

    @DisplayName("????????? ??????????????? ?????????, ????????? ????????????.")
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

    @DisplayName("??????????????? ????????? ??????, ????????? ??????????????? ????????????")
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

        Long postId = postService.create("seller", json, multipartFiles);

        //When
        this.mockMvc.perform(post("/post/{postId}/purchase", postId))
                .andExpect(status().isOk()); //Then

    }

    @DisplayName("????????? ?????? ???????????? ?????? ??????, ????????? ????????????.")
    @Test
    @WithMockCustomUser
    @Transactional
    void purchaseWhenPrice() throws Exception {

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

        Long postId = postService.create("seller", json, multipartFiles);

        //When
        this.mockMvc.perform(post("/post/{postId}/purchase", postId))
                .andExpect(status().isBadRequest()); //Then

    }

    @DisplayName("?????? ?????? ??? ??????????????? ????????? ??????, ????????? ????????????.")
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

        Long postId = postService.create("seller", json, multipartFiles);

        purchaseService.purchasePost("buyer1",postId);

        //When
        this.mockMvc.perform(post("/post/{postId}/purchase", postId))
                .andExpect(status().isBadRequest()); //Then

    }

}