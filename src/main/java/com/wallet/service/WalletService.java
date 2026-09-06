package com.wallet.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.wallet.dto.request.AddMoneyRequest;
import com.wallet.dto.request.TransferRequest;
import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.response.WalletResponsese;

public interface WalletService {

	WalletResponsese addMoney(AddMoneyRequest request, String idempotencyKey);

	TransactionResponse transfer(TransferRequest request, String idempotencyKey);

	WalletResponsese getWallet();

	Page<TransactionResponse> getTransactions(Pageable pageable);
}