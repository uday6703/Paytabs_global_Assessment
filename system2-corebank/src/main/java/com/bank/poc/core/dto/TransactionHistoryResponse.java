package com.bank.poc.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for transaction history display.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponse {
    
    private Long id;
    
    private String cardNumber;
    
    private String maskedCardNumber;
    
    private String type;
    
    private double amount;
    
    private LocalDateTime timestamp;
    
    private String status;
    
    private String reason;
}
