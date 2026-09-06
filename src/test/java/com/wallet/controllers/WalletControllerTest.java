package com.wallet.controllers;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.dto.request.AddMoneyRequest;
import com.wallet.dto.request.TransferRequest;
import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.response.WalletResponsese;
import com.wallet.repository.UserRepository;
import com.wallet.restcontroller.WalletController;
import com.wallet.security.JwtService;
import com.wallet.service.WalletService;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private WalletService walletService;

    @Test
    void addMoney_shouldReturn200() throws Exception {

        AddMoneyRequest request = AddMoneyRequest.builder()
                .amount(new BigDecimal("500.00"))
                .build();

        WalletResponsese response = WalletResponsese.builder()
                .walletId(1L)
                .balance(new BigDecimal("500.00"))
                .build();

        when(walletService.addMoney(
                any(AddMoneyRequest.class),
                anyString()))
                .thenReturn(response);

        mockMvc.perform(
                post("/wallet/add")
                        .header("Idempotency-Key", "ADD-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk());
    }

    @Test
    void transfer_shouldReturn200() throws Exception {

        TransferRequest request = TransferRequest.builder()
                .receiverUserId(2L)
                .amount(new BigDecimal("200.00"))
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .transactionId(1L)
                .amount(new BigDecimal("200.00"))
                .build();

        when(walletService.transfer(
                any(TransferRequest.class),
                anyString()))
                .thenReturn(response);

        mockMvc.perform(
                post("/wallet/transfer")
                        .header("Idempotency-Key", "TRANSFER-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk());
    }

    @Test
    void getWallet_shouldReturn200() throws Exception {

        WalletResponsese response = WalletResponsese.builder()
                .walletId(1L)
                .balance(new BigDecimal("500.00"))
                .build();

        when(walletService.getWallet())
                .thenReturn(response);

        mockMvc.perform(
                get("/wallet")
        )
        .andExpect(status().isOk());
    }

    @Test
    void getTransactions_shouldReturn200() throws Exception {

        Page<TransactionResponse> page =
                new PageImpl<>(Collections.emptyList());

        when(walletService.getTransactions(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                get("/wallet/transactions")
                        .param("page", "0")
                        .param("size", "10")
        )
        .andExpect(status().isOk());
    }
}