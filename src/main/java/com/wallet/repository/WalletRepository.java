package com.wallet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wallet.entity.Wallet;

import jakarta.persistence.LockModeType;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT w
            FROM Wallet w
            WHERE w.id = :walletId
            """)
    Optional<Wallet> findByIdForUpdate(
            @Param("walletId") Long walletId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT w
            FROM Wallet w
            WHERE w.user.id = :userId
            """)
    Optional<Wallet> findByUserIdForUpdate(
            @Param("userId") Long userId
    );
}