package com.wallet.restcontroller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wallet.dto.response.ApiResponse;
import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.response.WalletResponsese;
import com.wallet.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/wallets")
    public ResponseEntity<ApiResponse<Page<WalletResponsese>>> getAllWallets(
            Pageable pageable) {

        Page<WalletResponsese> response =
                adminService.getAllWallets(pageable);

        return ResponseEntity.ok(
                ApiResponse.<Page<WalletResponsese>>builder()
                        .success(true)
                        .message("Wallets retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAllTransactions(
            Pageable pageable) {

        Page<TransactionResponse> response =
                adminService.getAllTransactions(pageable);

        return ResponseEntity.ok(
                ApiResponse.<Page<TransactionResponse>>builder()
                        .success(true)
                        .message("Transactions retrieved successfully")
                        .data(response)
                        .build()
        );
    }
}