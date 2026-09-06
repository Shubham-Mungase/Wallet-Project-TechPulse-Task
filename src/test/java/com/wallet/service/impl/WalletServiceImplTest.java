package com.wallet.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.wallet.dto.request.AddMoneyRequest;
import com.wallet.dto.request.TransferRequest;
import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.response.WalletResponsese;
import com.wallet.entity.Transaction;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.enums.Role;
import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import com.wallet.exceptions.InsufficientBalanceException;
import com.wallet.exceptions.InvalidTransferException;
import com.wallet.exceptions.WalletNotFoundException;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.serviceimpl.WalletServiceImpl;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WalletServiceImpl walletService;


    // TEST DATA

    private User senderUser;
    private User receiverUser;

    private Wallet senderWallet;
    private Wallet receiverWallet;


    @BeforeEach
    void setUp() {

        senderUser = User.builder()
                .id(1L)
                .email("sender@gmail.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        receiverUser = User.builder()
                .id(2L)
                .email("receiver@gmail.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        senderWallet = Wallet.builder()
                .id(1L)
                .user(senderUser)
                .balance(new BigDecimal("1000.00"))
                .version(0L)
                .build();

        receiverWallet = Wallet.builder()
                .id(2L)
                .user(receiverUser)
                .balance(new BigDecimal("500.00"))
                .version(0L)
                .build();

        authenticate(senderUser);
    }


    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }


    private void authenticate(User user) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole().name()
                                )
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }


    // ADD MONEY


    @Test
    void addMoney_shouldIncreaseBalance() {

        AddMoneyRequest request = AddMoneyRequest.builder()
                .amount(new BigDecimal("200.00"))
                .build();

        when(transactionRepository.findByIdempotencyKey("ADD-001"))
                .thenReturn(Optional.empty());

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(senderWallet));

        Transaction savedTransaction = Transaction.builder()
                .walletId(1L)
                .toWallet(senderWallet)
                .amount(new BigDecimal("200.00"))
                .type(TransactionType.ADD)
                .status(TransactionStatus.SUCCESS)
                .idempotencyKey("ADD-001")
                .build();

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTransaction);


        WalletResponsese response =
                walletService.addMoney(request, "ADD-001");


        assertEquals(
                new BigDecimal("1200.00"),
                response.getBalance()
        );

        verify(walletRepository)
                .findByUserIdForUpdate(1L);

        verify(transactionRepository)
                .save(any(Transaction.class));
    }


    @Test
    void addMoney_shouldNotProcessAgain_whenIdempotencyKeyAlreadyExists() {

        Transaction existingTransaction = Transaction.builder()
                .walletId(1L)
                .toWallet(senderWallet)
                .amount(new BigDecimal("200.00"))
                .type(TransactionType.ADD)
                .status(TransactionStatus.SUCCESS)
                .idempotencyKey("ADD-001")
                .build();

        when(transactionRepository.findByIdempotencyKey("ADD-001"))
                .thenReturn(Optional.of(existingTransaction));


        WalletResponsese response =
                walletService.addMoney(
                        AddMoneyRequest.builder()
                                .amount(new BigDecimal("200.00"))
                                .build(),
                        "ADD-001"
                );


        assertEquals(
                new BigDecimal("1000.00"),
                response.getBalance()
        );

        verify(walletRepository, never())
                .findByUserIdForUpdate(any());

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    @Test
    void addMoney_shouldThrowException_whenWalletDoesNotExist() {

        when(transactionRepository.findByIdempotencyKey("ADD-002"))
                .thenReturn(Optional.empty());

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.empty());


        assertThrows(
                com.wallet.exceptions.WalletNotFoundException.class,
                () -> walletService.addMoney(
                        AddMoneyRequest.builder()
                                .amount(new BigDecimal("100.00"))
                                .build(),
                        "ADD-002"
                )
        );


        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    // TRANSFER


    @Test
    void transfer_shouldTransferMoneySuccessfully() {

        TransferRequest request = TransferRequest.builder()
                .receiverUserId(2L)
                .amount(new BigDecimal("300.00"))
                .build();

        when(transactionRepository.findByIdempotencyKey("TRANSFER-001"))
                .thenReturn(Optional.empty());

        when(walletRepository.findByUserId(1L))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUserId(2L))
                .thenReturn(Optional.of(receiverWallet));

        when(walletRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(receiverWallet));

        Transaction savedTransaction = Transaction.builder()
                .walletId(10L)
                .fromWallet(senderWallet)
                .toWallet(receiverWallet)
                .amount(new BigDecimal("300.00"))
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .idempotencyKey("TRANSFER-001")
                .build();

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTransaction);


        TransactionResponse response =
                walletService.transfer(
                        request,
                        "TRANSFER-001"
                );


        assertEquals(
                new BigDecimal("700.00"),
                senderWallet.getBalance()
        );

        assertEquals(
                new BigDecimal("800.00"),
                receiverWallet.getBalance()
        );

        assertEquals(
                10L,
                response.getTransactionId()
        );

        assertEquals(
                TransactionType.TRANSFER,
                response.getType()
        );

        verify(transactionRepository)
                .save(any(Transaction.class));
    }


    @Test
    void transfer_shouldThrowException_whenInsufficientBalance() {

        TransferRequest request = TransferRequest.builder()
                .receiverUserId(2L)
                .amount(new BigDecimal("1500.00"))
                .build();

        when(transactionRepository.findByIdempotencyKey("TRANSFER-002"))
                .thenReturn(Optional.empty());

        when(walletRepository.findByUserId(1L))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUserId(2L))
                .thenReturn(Optional.of(receiverWallet));

        when(walletRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(receiverWallet));


        assertThrows(
                InsufficientBalanceException.class,
                () -> walletService.transfer(
                        request,
                        "TRANSFER-002"
                )
        );


        assertEquals(
                new BigDecimal("1000.00"),
                senderWallet.getBalance()
        );

        assertEquals(
                new BigDecimal("500.00"),
                receiverWallet.getBalance()
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    @Test
    void transfer_shouldThrowException_whenTransferringToOwnWallet() {

        TransferRequest request = TransferRequest.builder()
                .receiverUserId(1L)
                .amount(new BigDecimal("100.00"))
                .build();


        assertThrows(
                InvalidTransferException.class,
                () -> walletService.transfer(
                        request,
                        "TRANSFER-003"
                )
        );


        verify(walletRepository, never())
                .findByUserId(any());

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    @Test
    void transfer_shouldThrowException_whenSenderWalletDoesNotExist() {

        TransferRequest request = TransferRequest.builder()
                .receiverUserId(2L)
                .amount(new BigDecimal("100.00"))
                .build();

        when(transactionRepository.findByIdempotencyKey("TRANSFER-004"))
                .thenReturn(Optional.empty());

        when(walletRepository.findByUserId(1L))
                .thenReturn(Optional.empty());


        assertThrows(
                WalletNotFoundException.class,
                () -> walletService.transfer(
                        request,
                        "TRANSFER-004"
                )
        );


        verify(walletRepository, never())
                .findByUserId(2L);

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    @Test
    void transfer_shouldThrowException_whenReceiverWalletDoesNotExist() {

        TransferRequest request = TransferRequest.builder()
                .receiverUserId(2L)
                .amount(new BigDecimal("100.00"))
                .build();

        when(transactionRepository.findByIdempotencyKey("TRANSFER-005"))
                .thenReturn(Optional.empty());

        when(walletRepository.findByUserId(1L))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUserId(2L))
                .thenReturn(Optional.empty());


        assertThrows(
                WalletNotFoundException.class,
                () -> walletService.transfer(
                        request,
                        "TRANSFER-005"
                )
        );


        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    @Test
    void transfer_shouldNotProcessAgain_whenIdempotencyKeyAlreadyExists() {

        Transaction existingTransaction = Transaction.builder()
                .walletId(10L)
                .fromWallet(senderWallet)
                .toWallet(receiverWallet)
                .amount(new BigDecimal("300.00"))
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .idempotencyKey("TRANSFER-006")
                .build();

        when(transactionRepository.findByIdempotencyKey("TRANSFER-006"))
                .thenReturn(Optional.of(existingTransaction));


        TransactionResponse response =
                walletService.transfer(
                        TransferRequest.builder()
                                .receiverUserId(2L)
                                .amount(new BigDecimal("300.00"))
                                .build(),
                        "TRANSFER-006"
                );


        assertEquals(
                10L,
                response.getTransactionId()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                senderWallet.getBalance()
        );

        assertEquals(
                new BigDecimal("500.00"),
                receiverWallet.getBalance()
        );

        verify(walletRepository, never())
                .findByUserId(any());

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    @Test
    void transfer_shouldAllowExactAvailableBalance() {

        TransferRequest request = TransferRequest.builder()
                .receiverUserId(2L)
                .amount(new BigDecimal("1000.00"))
                .build();

        when(transactionRepository.findByIdempotencyKey("TRANSFER-007"))
                .thenReturn(Optional.empty());

        when(walletRepository.findByUserId(1L))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUserId(2L))
                .thenReturn(Optional.of(receiverWallet));

        when(walletRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(receiverWallet));

        Transaction transaction = Transaction.builder()
                .walletId(20L)
                .fromWallet(senderWallet)
                .toWallet(receiverWallet)
                .amount(new BigDecimal("1000.00"))
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .idempotencyKey("TRANSFER-007")
                .build();

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);


        walletService.transfer(request, "TRANSFER-007");


        assertEquals(
                new BigDecimal("0.00"),
                senderWallet.getBalance()
        );

        assertEquals(
                new BigDecimal("1500.00"),
                receiverWallet.getBalance()
        );
    }


    // GET WALLET


    @Test
    void getWallet_shouldReturnCurrentWallet() {

        when(walletRepository.findByUserId(1L))
                .thenReturn(Optional.of(senderWallet));


        WalletResponsese response =
                walletService.getWallet();


        assertEquals(
                1L,
                response.getWalletId()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                response.getBalance()
        );

        verify(walletRepository)
                .findByUserId(1L);
    }


    @Test
    void getWallet_shouldThrowException_whenWalletDoesNotExist() {

        when(walletRepository.findByUserId(1L))
                .thenReturn(Optional.empty());


        assertThrows(
                WalletNotFoundException.class,
                () -> walletService.getWallet()
        );
    }


    // TRANSACTION HISTORY


    @Test
    void getTransactions_shouldReturnPaginatedTransactions() {

        PageRequest pageable =
                PageRequest.of(0, 10);

        Transaction transaction = Transaction.builder()
                .walletId(1L)
                .fromWallet(null)
                .toWallet(senderWallet)
                .amount(new BigDecimal("500.00"))
                .type(TransactionType.ADD)
                .status(TransactionStatus.SUCCESS)
                .idempotencyKey("ADD-001")
                .build();

        Page<Transaction> transactionPage =
                new PageImpl<>(
                        List.of(transaction),
                        pageable,
                        1
                );

        when(transactionRepository
                .findByFromWalletUserIdOrToWalletUserId(
                        1L,
                        1L,
                        pageable
                ))
                .thenReturn(transactionPage);


        Page<TransactionResponse> response =
                walletService.getTransactions(pageable);


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
                new BigDecimal("500.00"),
                response.getContent()
                        .get(0)
                        .getAmount()
        );

        verify(transactionRepository)
                .findByFromWalletUserIdOrToWalletUserId(
                        1L,
                        1L,
                        pageable
                );
    }


    @Test
    void getTransactions_shouldReturnEmptyPage_whenNoTransactionsExist() {

        PageRequest pageable =
                PageRequest.of(0, 10);

        Page<Transaction> emptyPage =
                new PageImpl<>(
                        List.of(),
                        pageable,
                        0
                );

        when(transactionRepository
                .findByFromWalletUserIdOrToWalletUserId(
                        1L,
                        1L,
                        pageable
                ))
                .thenReturn(emptyPage);


        Page<TransactionResponse> response =
                walletService.getTransactions(pageable);


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