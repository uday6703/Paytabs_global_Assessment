package com.bank.poc.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for incoming transaction requests.
 * PIN is received as plain text but immediately hashed for validation.
 * Plain text PIN is NEVER logged or stored.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    
    private String cardNumber;    // Card number (must start with 4)
    
    private String pin;           // PIN for authentication (will be hashed)
    
    private double amount;        // Transaction amount
    
    private String type;          // "withdraw" or "topup"
    
    // Custom toString to prevent PIN from being logged
    @Override
    public String toString() {
        return "TransactionRequest{" +
                "cardNumber='" + maskCardNumber(cardNumber) + '\'' +
                ", pin='****'" +  // Never log PIN
                ", amount=" + amount +
                ", type='" + type + '\'' +
                '}';
    }
    
    private String maskCardNumber(String cardNum) {
        if (cardNum == null || cardNum.length() < 4) return "****";
        return "****" + cardNum.substring(cardNum.length() - 4);
    }
}
