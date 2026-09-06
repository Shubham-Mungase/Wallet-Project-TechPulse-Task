package com.wallet.restcontroller;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wallet.dto.request.AddMoneyRequest;
import com.wallet.dto.request.TransferRequest;
import com.wallet.dto.response.ApiResponse;
import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.response.WalletResponsese;
import com.wallet.service.WalletService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<WalletResponsese>> addMoney(
            @Valid @RequestBody AddMoneyRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        WalletResponsese response =
                walletService.addMoney(request, idempotencyKey);

        return ResponseEntity.ok(
                ApiResponse.<WalletResponsese>builder()
                        .success(true)
                        .message("Money added successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        TransactionResponse response =
                walletService.transfer(request, idempotencyKey);

        return ResponseEntity.ok(
                ApiResponse.<TransactionResponse>builder()
                        .success(true)
                        .message("Money transferred successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<WalletResponsese>> getWallet() {

        WalletResponsese response =
                walletService.getWallet();

        return ResponseEntity.ok(
                ApiResponse.<WalletResponsese>builder()
                        .success(true)
                        .message("Wallet retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            Pageable pageable) {

        Page<TransactionResponse> response =
                walletService.getTransactions(pageable);

        return ResponseEntity.ok(
                ApiResponse.<Page<TransactionResponse>>builder()
                        .success(true)
                        .message("Transactions retrieved successfully")
                        .data(response)
                        .build()
        );
    }
}
