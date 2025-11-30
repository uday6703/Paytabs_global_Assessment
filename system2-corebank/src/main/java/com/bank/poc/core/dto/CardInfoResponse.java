package com.bank.poc.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for card information response (excludes sensitive data like PIN hash).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardInfoResponse {
    
    private String cardNumber;
    
    private String maskedCardNumber;  // e.g., "****1234"
    
    private double balance;
    
    private String customerName;
    
    private String username;
    
    public static CardInfoResponse fromCard(String cardNumber, double balance, 
                                            String customerName, String username) {
        String masked = "****" + cardNumber.substring(cardNumber.length() - 4);
        return new CardInfoResponse(cardNumber, masked, balance, customerName, username);
    }
}
