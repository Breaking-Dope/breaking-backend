package com.dope.breaking.service;

import com.dope.breaking.domain.financial.TransactionType;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.financial.TransactionResponseDto;
import com.dope.breaking.repository.UserRepository;
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
class TransactionServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private StatementService statementService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private PostService postService;


    @DisplayName("유저네임이 일치할 경우, 입/출금과 제보 구매/판매 내역이 최신순으로 리턴된다.")
    @Test
    void transactionList() throws Exception {

        //Given
        User user1 = new User();
        user1.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username1", Role.USER);
        userRepository.save(user1);

        statementService.depositOrWithdraw("username1", 30000, TransactionType.DEPOSIT);
        statementService.depositOrWithdraw("username1", 10000, TransactionType.WITHDRAW);


        User user2 = new User();
        user2.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "username2", Role.USER);
        user2.updateBalance(0);
        userRepository.save(user2);

        List<MultipartFile> multipartFiles = new LinkedList<>();

        String json1 = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 10000," +
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

        Long postId1 = postService.create("username2", json1, multipartFiles);
        purchaseService.purchasePost("username1",postId1);

        String json2 = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 5000," +
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

        Long postId2 = postService.create("username1", json2, multipartFiles);
        purchaseService.purchasePost("username2",postId2);

        //When
        List<TransactionResponseDto> transactionList = transactionService.transactionList("username1");

        //Then
        assertEquals("sell_post",transactionList.get(0).getTransactionType());
        assertEquals("buy_post",transactionList.get(1).getTransactionType());
        assertEquals("withdraw",transactionList.get(2).getTransactionType());
        assertEquals("deposit",transactionList.get(3).getTransactionType());

    }

    @DisplayName("아무 입출금 및 거래내역이 없는 경우, 빈 리스트를 반환한다. ")
    @Test
    void emptyTransactionList() throws Exception {

        //Given
        User user1 = new User();
        user1.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "username1", Role.USER);
        userRepository.save(user1);

        //When
        List<TransactionResponseDto> transactionList = transactionService.transactionList("username1");

        //Then
        assertEquals(0, transactionList.size());
    }

}