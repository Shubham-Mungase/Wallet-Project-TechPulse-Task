package com.wallet.serviceimpl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.response.WalletResponsese;
import com.wallet.entity.Transaction;
import com.wallet.entity.Wallet;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.service.AdminService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public Page<WalletResponsese> getAllWallets(Pageable pageable) {

        Page<Wallet> wallets =
                walletRepository.findAll(pageable);

        return wallets.map(wallet ->
                WalletResponsese.builder()
                        .walletId(wallet.getId())
                        .balance(wallet.getBalance())
                        .build()
        );
    }

    @Override
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {

        Page<Transaction> transactions =
                transactionRepository.findAll(pageable);

        return transactions.map(this::mapToTransactionResponse);
    }

    private TransactionResponse mapToTransactionResponse(
            Transaction transaction) {

        return TransactionResponse.builder()
                .transactionId(transaction.getWalletId())
                .fromWalletId(
                        transaction.getFromWallet() != null
                                ? transaction.getFromWallet().getId()
                                : null)
                .toWalletId(
                        transaction.getToWallet() != null
                                ? transaction.getToWallet().getId()
                                : null)
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .idempotencyKey(transaction.getIdempotencyKey())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}