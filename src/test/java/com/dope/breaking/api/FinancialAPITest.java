package com.dope.breaking.api;

import com.dope.breaking.domain.financial.TransactionType;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.financial.AmountRequestDto;
import com.dope.breaking.repository.StatementRepository;
import com.dope.breaking.repository.TransactionRepository;
import com.dope.breaking.repository.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private StatementService statementService;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    public void createUserInfo() {

        User user = User.builder()
                .username("12345g")
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.USER) // 최초 가입시 USER 로 설정
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
    
}