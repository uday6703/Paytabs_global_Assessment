package com.bank.poc.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transaction responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    
    private boolean success;
    
    private String message;
    
    private Double newBalance;    // New balance after transaction (null if failed)
    
    private Long transactionId;   // Transaction ID for reference
    
    public static TransactionResponse success(String message, double newBalance, Long transactionId) {
        return new TransactionResponse(true, message, newBalance, transactionId);
    }
    
    public static TransactionResponse error(String message) {
        return new TransactionResponse(false, message, null, null);
    }
}
