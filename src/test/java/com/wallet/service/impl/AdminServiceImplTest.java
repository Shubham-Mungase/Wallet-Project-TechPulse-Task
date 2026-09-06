package com.wallet.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.wallet.entity.Transaction;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.enums.Role;
import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.serviceimpl.AdminServiceImpl;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AdminServiceImpl adminService;


    @Test
    void getAllWallets_shouldReturnPaginatedWallets() {

        // ARRANGE

        PageRequest pageable = PageRequest.of(0, 10);

        User user = User.builder()
                .id(1L)
                .email("user@gmail.com")
                .role(Role.USER)
                .build();

        Wallet wallet = Wallet.builder()
                .id(1L)
                .user(user)
                .balance(new BigDecimal("1000.00"))
                .version(0L)
                .build();

        Page<Wallet> walletPage =
                new PageImpl<>(
                        List.of(wallet),
                        pageable,
                        1
                );

        when(walletRepository.findAll(pageable))
                .thenReturn(walletPage);


        // ACT

        Page<com.wallet.dto.response.WalletResponsese> response =
                adminService.getAllWallets(pageable);


        // ASSERT

        assertEquals(
                1,
                response.getTotalElements()
        );

        assertEquals(
                1L,
                response.getContent()
                        .get(0)
                        .getWalletId()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                response.getContent()
                        .get(0)
                        .getBalance()
        );


        // VERIFY

        verify(walletRepository)
                .findAll(pageable);
    }


    @Test
    void getAllWallets_shouldReturnEmptyPage_whenNoWalletsExist() {

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Wallet> emptyPage =
                new PageImpl<>(
                        List.of(),
                        pageable,
                        0
                );

        when(walletRepository.findAll(pageable))
                .thenReturn(emptyPage);


        Page<com.wallet.dto.response.WalletResponsese> response =
                adminService.getAllWallets(pageable);


        assertEquals(
                0,
                response.getTotalElements()
        );

        assertEquals(
                0,
                response.getContent().size()
        );
    }


    @Test
    void getAllTransactions_shouldReturnPaginatedTransactions() {

        // ARRANGE

        PageRequest pageable = PageRequest.of(0, 10);

        User sender = User.builder()
                .id(1L)
                .email("sender@gmail.com")
                .role(Role.USER)
                .build();

        User receiver = User.builder()
                .id(2L)
                .email("receiver@gmail.com")
                .role(Role.USER)
                .build();

        Wallet senderWallet = Wallet.builder()
                .id(1L)
                .user(sender)
                .balance(new BigDecimal("700.00"))
                .version(0L)
                .build();

        Wallet receiverWallet = Wallet.builder()
                .id(2L)
                .user(receiver)
                .balance(new BigDecimal("800.00"))
                .version(0L)
                .build();

        Transaction transaction = Transaction.builder()
                .walletId(1L)
                .fromWallet(senderWallet)
                .toWallet(receiverWallet)
                .amount(new BigDecimal("300.00"))
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .idempotencyKey("TRANSFER-001")
                .build();

        Page<Transaction> transactionPage =
                new PageImpl<>(
                        List.of(transaction),
                        pageable,
                        1
                );

        when(transactionRepository.findAll(pageable))
                .thenReturn(transactionPage);


        // ACT

        Page<com.wallet.dto.response.TransactionResponse> response =
                adminService.getAllTransactions(pageable);


        // ASSERT

        assertEquals(
                1,
                response.getTotalElements()
        );

        assertEquals(
                1L,
                response.getContent()
                        .get(0)
                        .getTransactionId()
        );

        assertEquals(
                new BigDecimal("300.00"),
                response.getContent()
                        .get(0)
                        .getAmount()
        );

        assertEquals(
                TransactionType.TRANSFER,
                response.getContent()
                        .get(0)
                        .getType()
        );


        // VERIFY

        verify(transactionRepository)
                .findAll(pageable);
    }


    @Test
    void getAllTransactions_shouldReturnEmptyPage_whenNoTransactionsExist() {

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Transaction> emptyPage =
                new PageImpl<>(
                        List.of(),
                        pageable,
                        0
                );

        when(transactionRepository.findAll(pageable))
                .thenReturn(emptyPage);


        Page<com.wallet.dto.response.TransactionResponse> response =
                adminService.getAllTransactions(pageable);


        assertEquals(
                0,
                response.getTotalElements()
        );

        assertEquals(
                0,
                response.getContent().size()
        );
    }
}