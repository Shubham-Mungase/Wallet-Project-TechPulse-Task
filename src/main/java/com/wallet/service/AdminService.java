package com.wallet.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.response.WalletResponsese;

public interface AdminService {

    Page<WalletResponsese> getAllWallets(Pageable pageable);

    Page<TransactionResponse> getAllTransactions(Pageable pageable);
}