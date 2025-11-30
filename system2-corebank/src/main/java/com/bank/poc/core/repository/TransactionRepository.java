package com.bank.poc.core.repository;

import com.bank.poc.core.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Transaction entity operations.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * Find all transactions for a specific card, ordered by timestamp descending.
     */
    List<Transaction> findByCardNumberOrderByTimestampDesc(String cardNumber);
    
    /**
     * Find all transactions ordered by timestamp descending (for admin view).
     */
    List<Transaction> findAllByOrderByTimestampDesc();
}
