package com.wallet.serviceimpl;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wallet.dto.request.AddMoneyRequest;
import com.wallet.dto.request.TransferRequest;
import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.response.WalletResponsese;
import com.wallet.entity.Transaction;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import com.wallet.exceptions.InsufficientBalanceException;
import com.wallet.exceptions.InvalidTransferException;
import com.wallet.exceptions.WalletNotFoundException;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.service.WalletService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletServiceImpl implements WalletService {

	private final TransactionRepository transactionRepository;
	private final WalletRepository walletRepository;

	@Override
	@Transactional
	public WalletResponsese addMoney(AddMoneyRequest request, String idempotencyKey) {

		// Check whether this idempotency key was already processed
		Optional<Transaction> existingTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);

		if (existingTransaction.isPresent()) {

			Wallet wallet = existingTransaction.get().getToWallet();

			return WalletResponsese.builder().walletId(wallet.getId()).balance(wallet.getBalance()).build();
		}

		//  Get authenticated user

		@Nullable
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) authentication.getPrincipal();

		//  Lock user's wallet
		Wallet wallet = walletRepository.findByUserIdForUpdate(user.getId())
				.orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

		// Update balance
		wallet.setBalance(wallet.getBalance().add(request.getAmount()));

		//  Create transaction record
		Transaction transaction = Transaction.builder().fromWallet(null).toWallet(wallet).amount(request.getAmount())
				.type(TransactionType.ADD).status(TransactionStatus.SUCCESS).idempotencyKey(idempotencyKey).build();

		transactionRepository.save(transaction);

		// 6. Return updated wallet
		return WalletResponsese.builder().walletId(wallet.getId()).balance(wallet.getBalance()).build();
	}

	@Override
	@Transactional
	public TransactionResponse transfer(TransferRequest request, String idempotencyKey) {

		//  Idempotency check
		Optional<Transaction> existingTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);

		if (existingTransaction.isPresent()) {
			return mapToTransactionResponse(existingTransaction.get());
		}

		//  Get authenticated user
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		User senderUser = (User) authentication.getPrincipal();

		Long senderUserId = senderUser.getId();

		//  Prevent self-transfer
		if (senderUserId.equals(request.getReceiverUserId())) {
			throw new InvalidTransferException("Cannot transfer money to your own wallet");
		}

		//  Find wallets
		Wallet senderWallet = walletRepository.findByUserId(senderUserId)
				.orElseThrow(() -> new WalletNotFoundException("Sender wallet not found"));

		Wallet receiverWallet = walletRepository.findByUserId(request.getReceiverUserId())
				.orElseThrow(() -> new WalletNotFoundException("Receiver wallet not found"));

		//  Lock wallets in deterministic order
		Wallet firstWallet;
		Wallet secondWallet;

		if (senderWallet.getId() < receiverWallet.getId()) {

			firstWallet = walletRepository.findByIdForUpdate(senderWallet.getId()).orElseThrow(() ->
		    new WalletNotFoundException("Sender wallet not found"));

			secondWallet = walletRepository.findByIdForUpdate(receiverWallet.getId()).orElseThrow(() ->
		    new WalletNotFoundException("Sender wallet not found"));
		} else {

			firstWallet = walletRepository.findByIdForUpdate(receiverWallet.getId()).orElseThrow(() ->
		    new WalletNotFoundException("Sender wallet not found"));

			secondWallet = walletRepository.findByIdForUpdate(senderWallet.getId()).orElseThrow(() ->
		    new WalletNotFoundException("Sender wallet not found"));
		}

		// Identify locked sender/receiver
		Wallet lockedSenderWallet = firstWallet.getId().equals(senderWallet.getId()) ? firstWallet : secondWallet;

		Wallet lockedReceiverWallet = firstWallet.getId().equals(receiverWallet.getId()) ? firstWallet : secondWallet;

		// Check balance
		if (lockedSenderWallet.getBalance().compareTo(request.getAmount()) < 0) {

			throw new InsufficientBalanceException("Insufficient wallet balance");
		}

		//  Deduct from sender
		lockedSenderWallet.setBalance(lockedSenderWallet.getBalance().subtract(request.getAmount()));

		// Add to receiver
		lockedReceiverWallet.setBalance(lockedReceiverWallet.getBalance().add(request.getAmount()));

		// Create transaction
		Transaction transaction = Transaction.builder().fromWallet(lockedSenderWallet).toWallet(lockedReceiverWallet)
				.amount(request.getAmount()).type(TransactionType.TRANSFER).status(TransactionStatus.SUCCESS)
				.idempotencyKey(idempotencyKey).build();

		Transaction savedTransaction = transactionRepository.save(transaction);

		// Return response
		return mapToTransactionResponse(savedTransaction);
	}

	@Override
	public WalletResponsese getWallet() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		User user = (User) authentication.getPrincipal();

		Wallet wallet = walletRepository.findByUserId(user.getId())
				.orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

		return WalletResponsese.builder().walletId(wallet.getId()).balance(wallet.getBalance()).build();
	}

	@Override
	public Page<TransactionResponse> getTransactions(Pageable pageable) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		User user = (User) authentication.getPrincipal();

		Long userId = user.getId();

		Page<Transaction> transactions = transactionRepository.findByFromWalletUserIdOrToWalletUserId(userId, userId,
				pageable);

		return transactions.map(this::mapToTransactionResponse);
	}

	private TransactionResponse mapToTransactionResponse(Transaction transaction) {

		return TransactionResponse.builder().transactionId(transaction.getWalletId())
				.fromWalletId(transaction.getFromWallet() != null ? transaction.getFromWallet().getId() : null)
				.toWalletId(transaction.getToWallet() != null ? transaction.getToWallet().getId() : null)
				.amount(transaction.getAmount()).type(transaction.getType()).status(transaction.getStatus())
				.idempotencyKey(transaction.getIdempotencyKey()).createdAt(transaction.getCreatedAt()).build();
	}

}