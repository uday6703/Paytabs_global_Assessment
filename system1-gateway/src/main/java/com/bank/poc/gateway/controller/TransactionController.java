package com.bank.poc.gateway.controller;

import com.bank.poc.gateway.dto.TransactionRequest;
import com.bank.poc.gateway.dto.TransactionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Gateway controller for handling incoming transactions.
 * Validates card range (must start with '4') and forwards to System 2.
 */
@RestController
@Slf4j
@CrossOrigin(origins = "*")  // Allow CORS for React frontend
public class TransactionController {

    private final RestTemplate restTemplate;
    
    @Value("${system2.url:http://localhost:8082}")
    private String system2Url;

    public TransactionController() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Main transaction endpoint.
     * POST /transaction
     * 
     * Validates:
     * - Card number is not empty
     * - Card number starts with '4' (card range check)
     * - PIN is not empty
     * - Amount is positive
     * - Type is 'withdraw' or 'topup'
     * 
     * Then forwards to System 2 for processing.
     */
    @PostMapping("/transaction")
    public ResponseEntity<TransactionResponse> handleTransaction(
            @RequestBody TransactionRequest request) {
        
        log.info("Received transaction request: {}", request);

        // Validation 1: Card number is required
        if (request.getCardNumber() == null || request.getCardNumber().isBlank()) {
            log.warn("Transaction rejected: Card number is required");
            return ResponseEntity.badRequest()
                .body(TransactionResponse.error("Card number is required"));
        }

        // Validation 2: Card must start with '4' (card range routing)
        if (!request.getCardNumber().startsWith("4")) {
            log.warn("Transaction rejected: Card range not supported (must start with 4)");
            return ResponseEntity.badRequest()
                .body(TransactionResponse.error("Card range not supported. Only cards starting with '4' are accepted."));
        }

        // Validation 3: Card number must be 16 digits
        if (request.getCardNumber().length() != 16 || 
            !request.getCardNumber().matches("\\d{16}")) {
            log.warn("Transaction rejected: Invalid card number format");
            return ResponseEntity.badRequest()
                .body(TransactionResponse.error("Card number must be exactly 16 digits"));
        }

        // Validation 4: PIN is required
        if (request.getPin() == null || request.getPin().isBlank()) {
            log.warn("Transaction rejected: PIN is required");
            return ResponseEntity.badRequest()
                .body(TransactionResponse.error("PIN is required"));
        }

        // Validation 5: Amount must be positive
        if (request.getAmount() <= 0) {
            log.warn("Transaction rejected: Amount must be positive");
            return ResponseEntity.badRequest()
                .body(TransactionResponse.error("Amount must be greater than 0"));
        }

        // Validation 6: Transaction type is required
        if (request.getType() == null || request.getType().isBlank()) {
            log.warn("Transaction rejected: Transaction type is required");
            return ResponseEntity.badRequest()
                .body(TransactionResponse.error("Transaction type is required"));
        }

        // Validation 7: Transaction type must be 'withdraw' or 'topup'
        String type = request.getType().toLowerCase();
        if (!type.equals("withdraw") && !type.equals("topup")) {
            log.warn("Transaction rejected: Invalid transaction type");
            return ResponseEntity.badRequest()
                .body(TransactionResponse.error("Invalid transaction type. Use 'withdraw' or 'topup'."));
        }

        // Forward to System 2 for processing
        try {
            log.info("Forwarding transaction to System 2: {}", system2Url + "/process");
            TransactionResponse response = restTemplate.postForObject(
                system2Url + "/process",
                request,
                TransactionResponse.class
            );
            
            log.info("Response from System 2: success={}", response != null && response.isSuccess());
            return ResponseEntity.ok(response);
            
        } catch (RestClientException e) {
            log.error("Failed to connect to System 2: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(TransactionResponse.error("Unable to process transaction. Core banking system unavailable."));
        }
    }

    /**
     * Health check endpoint.
     * GET /health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("System 1 - Gateway is running");
    }

    /**
     * Check if System 2 is available.
     * GET /health/system2
     */
    @GetMapping("/health/system2")
    public ResponseEntity<String> checkSystem2() {
        try {
            String response = restTemplate.getForObject(
                system2Url + "/health", String.class);
            return ResponseEntity.ok("System 2 Status: " + response);
        } catch (RestClientException e) {
            return ResponseEntity.internalServerError()
                .body("System 2 is unavailable: " + e.getMessage());
        }
    }
}
