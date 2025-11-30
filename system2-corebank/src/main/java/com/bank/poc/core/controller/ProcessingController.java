package com.bank.poc.core.controller;

import com.bank.poc.core.dto.*;
import com.bank.poc.core.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for transaction processing.
 * This is the main entry point for System 2 (Core Banking).
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")  // Allow CORS for React frontend
public class ProcessingController {

    private final CardService cardService;

    /**
     * Process a transaction (called from System 1 Gateway).
     * POST /process
     */
    @PostMapping("/process")
    public ResponseEntity<TransactionResponse> processTransaction(
            @RequestBody TransactionRequest request) {
        
        log.info("Received transaction request: {}", request);
        
        // Validate required fields
        if (request.getCardNumber() == null || request.getCardNumber().isBlank()) {
            return ResponseEntity.badRequest()
                .body(TransactionResponse.error("Card number is required"));
        }
        if (request.getPin() == null || request.getPin().isBlank()) {
            return ResponseEntity.badRequest()
                .body(TransactionResponse.error("PIN is required"));
        }
        if (request.getAmount() <= 0) {
            return ResponseEntity.badRequest()
                .body(TransactionResponse.error("Amount must be greater than 0"));
        }
        if (request.getType() == null || request.getType().isBlank()) {
            return ResponseEntity.badRequest()
                .body(TransactionResponse.error("Transaction type is required"));
        }

        TransactionResponse response = cardService.processTransaction(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get card info by username (for customer login).
     * GET /card/by-username/{username}
     */
    @GetMapping("/card/by-username/{username}")
    public ResponseEntity<CardInfoResponse> getCardByUsername(@PathVariable String username) {
        return cardService.getCardByUsername(username)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get card info by card number.
     * GET /card/{cardNumber}
     */
    @GetMapping("/card/{cardNumber}")
    public ResponseEntity<CardInfoResponse> getCard(@PathVariable String cardNumber) {
        return cardService.getCardByCardNumber(cardNumber)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get transaction history for a specific card.
     * GET /transactions/{cardNumber}
     */
    @GetMapping("/transactions/{cardNumber}")
    public ResponseEntity<List<TransactionHistoryResponse>> getTransactionHistory(
            @PathVariable String cardNumber) {
        List<TransactionHistoryResponse> history = cardService.getTransactionHistory(cardNumber);
        return ResponseEntity.ok(history);
    }

    /**
     * Get all transactions (for admin dashboard).
     * GET /transactions/all
     */
    @GetMapping("/transactions/all")
    public ResponseEntity<List<TransactionHistoryResponse>> getAllTransactions() {
        List<TransactionHistoryResponse> allTransactions = cardService.getAllTransactions();
        return ResponseEntity.ok(allTransactions);
    }

    /**
     * Health check endpoint.
     * GET /health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("System 2 - Core Banking is running");
    }
}
