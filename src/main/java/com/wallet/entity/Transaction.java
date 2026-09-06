package com.wallet.entity;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "transactions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_transaction_idempotency_key",
            columnNames = "idempotency_key"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_wallet")
    private Wallet fromWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_wallet")
    private Wallet toWallet;

    @Column(
        nullable = false,
        precision = 19,
        scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 20
    )
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 20
    )
    private TransactionStatus status;

    @Column(
        name = "idempotency_key",
        nullable = false,
        unique = true,
        length = 100
    )
    private String idempotencyKey;

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}