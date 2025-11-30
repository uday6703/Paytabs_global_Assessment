package com.bank.poc.gateway.dto;

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
    
    private Double newBalance;
    
    private Long transactionId;
    
    public static TransactionResponse error(String message) {
        return new TransactionResponse(false, message, null, null);
    }
}
