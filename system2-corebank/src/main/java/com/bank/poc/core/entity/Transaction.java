package com.bank.poc.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Transaction entity for logging all banking transactions.
 * Stores complete audit trail of withdrawals and top-ups.
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String cardNumber;        // Card involved in transaction
    
    private String type;              // "withdraw" or "topup"
    
    private double amount;            // Transaction amount
    
    private LocalDateTime timestamp;  // When transaction occurred
    
    private String status;            // SUCCESS or FAILED
    
    private String reason;            // Failure reason or success message
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
