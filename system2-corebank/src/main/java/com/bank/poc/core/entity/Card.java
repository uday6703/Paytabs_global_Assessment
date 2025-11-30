package com.bank.poc.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Card entity representing a bank card in the system.
 * 
 * SECURITY FEATURES:
 * - PIN is stored as SHA-256 hash - NEVER in plain text
 * - Card number is stored encrypted using AES-256
 * - Sensitive data is never logged in plain text
 */
@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    
    @Id
    private String cardNumber;              // Primary key (plain for lookups)
    
    @Column(name = "card_number_encrypted")
    private String cardNumberEncrypted;     // AES-256 encrypted card number for secure storage
    
    private String pinHash;                 // SHA-256 hash - NEVER store plain text PIN
    
    private double balance;                 // Current account balance
    
    private String customerName;            // Customer full name
    
    private String username;                // Login username for the customer
    
    private boolean active = true;          // Card status
    
    /**
     * Custom toString to prevent logging sensitive data.
     */
    @Override
    public String toString() {
        return "Card{" +
                "cardNumber='****" + (cardNumber != null && cardNumber.length() >= 4 ? 
                    cardNumber.substring(cardNumber.length() - 4) : "") + '\'' +
                ", balance=" + balance +
                ", customerName='" + customerName + '\'' +
                ", username='" + username + '\'' +
                ", active=" + active +
                '}';
    }
}
