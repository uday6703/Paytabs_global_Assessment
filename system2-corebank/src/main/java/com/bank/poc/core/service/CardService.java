package com.bank.poc.core.service;

import com.bank.poc.core.dto.*;
import com.bank.poc.core.entity.Card;
import com.bank.poc.core.entity.Transaction;
import com.bank.poc.core.repository.CardRepository;
import com.bank.poc.core.repository.TransactionRepository;
import com.bank.poc.core.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Core service for card operations and transaction processing.
 * 
 * SECURITY FEATURES:
 * - PIN hashing using SHA-256
 * - Card number encryption using AES-256
 * - No plain-text sensitive data in logs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final CryptoUtil cryptoUtil;

    /**
     * Hash a PIN using SHA-256.
     * CRITICAL: Never log or store the plain text PIN!
     */
    public String hashPin(String pin) {
        return DigestUtils.sha256Hex(pin);
    }

    /**
     * Verify if the provided PIN matches the stored hash.
     */
    public boolean verifyPin(String inputPin, String storedHash) {
        return storedHash.equals(hashPin(inputPin));
    }

    /**
     * Process a transaction (withdraw or topup).
     * This is the main transaction processing logic.
     * 
     * Validation Flow:
     * 1. Check if card exists in database
     * 2. Validate PIN using SHA-256 hash comparison
     * 3. Check balance for withdrawals
     * 4. Process transaction and update balance
     */
    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {
        log.info("Processing transaction: {}", request); // PIN is masked in toString

        // 1. Find the card - Check if card number exists in the database
        Optional<Card> cardOpt = cardRepository.findById(request.getCardNumber());
        if (cardOpt.isEmpty()) {
            log.warn("Invalid card: ****{}", 
                CryptoUtil.maskCardNumber(request.getCardNumber()));
            return saveAndReturnError(request, "Invalid card");
        }

        Card card = cardOpt.get();
        
        // Check if card is active
        if (!card.isActive()) {
            log.warn("Card is inactive: {}", CryptoUtil.maskCardNumber(request.getCardNumber()));
            return saveAndReturnError(request, "Card is inactive");
        }

        // 2. Validate PIN (using SHA-256 hash comparison)
        if (!verifyPin(request.getPin(), card.getPinHash())) {
            log.warn("Invalid PIN attempt for card: {}", 
                CryptoUtil.maskCardNumber(request.getCardNumber()));
            return saveAndReturnError(request, "Invalid PIN");
        }

        // 3. Process based on transaction type
        String type = request.getType().toLowerCase();
        double amount = request.getAmount();

        if ("withdraw".equals(type)) {
            // Check sufficient balance for withdrawal
            if (card.getBalance() < amount) {
                log.warn("Insufficient balance for withdrawal. Card: {}, Balance: {}, Requested: {}",
                    CryptoUtil.maskCardNumber(request.getCardNumber()), card.getBalance(), amount);
                return saveAndReturnError(request, "Insufficient balance");
            }
            card.setBalance(card.getBalance() - amount);
        } else if ("topup".equals(type)) {
            card.setBalance(card.getBalance() + amount);
        } else {
            return saveAndReturnError(request, "Invalid transaction type. Use 'withdraw' or 'topup'");
        }

        // 4. Save updated card
        cardRepository.save(card);

        // 5. Log successful transaction
        Transaction transaction = new Transaction();
        transaction.setCardNumber(request.getCardNumber());
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setStatus("SUCCESS");
        transaction.setReason("Transaction completed successfully");
        transactionRepository.save(transaction);

        log.info("Transaction successful. Card: {}, Type: {}, Amount: {}, New Balance: {}", 
            CryptoUtil.maskCardNumber(request.getCardNumber()), type, amount, card.getBalance());

        return TransactionResponse.success(
            type.equals("withdraw") ? "Withdrawal successful" : "Top-up successful",
            card.getBalance(),
            transaction.getId()
        );
    }

    /**
     * Save a failed transaction and return error response.
     */
    private TransactionResponse saveAndReturnError(TransactionRequest request, String reason) {
        Transaction transaction = new Transaction();
        transaction.setCardNumber(request.getCardNumber());
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setStatus("FAILED");
        transaction.setReason(reason);
        transactionRepository.save(transaction);

        return TransactionResponse.error(reason);
    }

    /**
     * Get card info by username (for customer dashboard).
     */
    public Optional<CardInfoResponse> getCardByUsername(String username) {
        return cardRepository.findByUsername(username)
            .map(card -> CardInfoResponse.fromCard(
                card.getCardNumber(),
                card.getBalance(),
                card.getCustomerName(),
                card.getUsername()
            ));
    }

    /**
     * Get transaction history for a specific card.
     */
    public List<TransactionHistoryResponse> getTransactionHistory(String cardNumber) {
        return transactionRepository.findByCardNumberOrderByTimestampDesc(cardNumber)
            .stream()
            .map(this::mapToHistoryResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get all transactions (for admin dashboard).
     */
    public List<TransactionHistoryResponse> getAllTransactions() {
        return transactionRepository.findAllByOrderByTimestampDesc()
            .stream()
            .map(this::mapToHistoryResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get card info by card number.
     */
    public Optional<CardInfoResponse> getCardByCardNumber(String cardNumber) {
        return cardRepository.findById(cardNumber)
            .map(card -> CardInfoResponse.fromCard(
                card.getCardNumber(),
                card.getBalance(),
                card.getCustomerName(),
                card.getUsername()
            ));
    }

    private TransactionHistoryResponse mapToHistoryResponse(Transaction tx) {
        String masked = CryptoUtil.maskCardNumber(tx.getCardNumber());
        return new TransactionHistoryResponse(
            tx.getId(),
            tx.getCardNumber(),
            masked,
            tx.getType(),
            tx.getAmount(),
            tx.getTimestamp(),
            tx.getStatus(),
            tx.getReason()
        );
    }
}
