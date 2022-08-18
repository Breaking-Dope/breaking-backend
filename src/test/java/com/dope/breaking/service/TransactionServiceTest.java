package com.dope.breaking.service;

import com.dope.breaking.domain.financial.TransactionType;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.financial.TransactionInfoResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
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

    @DisplayName("아무 입출금 및 거래내역이 없는 경우, 빈 리스트를 반환한다.")
    @Test
    void emptyTransactionList() throws Exception {

        //Given
        User user1 = new User();
        user1.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "username1", Role.USER);
        userRepository.save(user1);

        //When
        List<TransactionInfoResponseDto> transactionList = transactionService.transactionInfoList("username1",null,10);

        //Then
        assertEquals(0, transactionList.size());

    }

    @DisplayName("유저네임이 일치하지 않을 경우, 예외가 발생한다.")
    @Test
    void transactionListWithInvalidUsername(){

        //Given
        User user1 = new User();
        user1.setRequestFields("URL", "anyURL", "nickname", "01012345678", "mwk300@nyu.edu", "Minwu Kim", "msg", "username1", Role.USER);
        userRepository.save(user1);

        //Then
        Assertions.assertThrows(InvalidAccessTokenException.class,
                ()-> transactionService.transactionInfoList("invalidUsername",null,10)); //When

    }

    @DisplayName("유저네임이 일치할 경우, 입/출금과 제보 구매/판매 내역이 최신순으로 리턴된다.")
    @Test
    void newTransactionList() throws Exception {

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

        Long postId1 = postService.create("username2", json1, multipartFiles);
        purchaseService.purchasePost("username1",postId1);

        String json2 = "{" +
                "\"title\" : \"hello\"," +
                "\"content\" : \"content\"," +
                "\"price\" : 5000," +
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

        Long postId2 = postService.create("username1", json2, multipartFiles);
        purchaseService.purchasePost("username2",postId2);

        //When
        List<TransactionInfoResponseDto> transactionList = transactionService.transactionInfoList("username1",null, 5);

        //Then
        Assertions.assertEquals(4,transactionList.size());
        Assertions.assertEquals(TransactionType.SELL_POST.getTitle(),transactionList.get(0).getTransactionType());
        Assertions.assertEquals(TransactionType.BUY_POST.getTitle(),transactionList.get(1).getTransactionType());
        Assertions.assertEquals(TransactionType.WITHDRAW.getTitle(),transactionList.get(2).getTransactionType());
        Assertions.assertEquals(TransactionType.DEPOSIT.getTitle(),transactionList.get(3).getTransactionType());

    }

}