package com.wallet.dto.response;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long transactionId;

    private Long fromWalletId;

    private Long toWalletId;

    private BigDecimal amount;

    private TransactionType type;

    private TransactionStatus status;

    private String idempotencyKey;

    private LocalDateTime createdAt;
}