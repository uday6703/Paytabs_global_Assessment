package com.bank.poc.core.repository;

import com.bank.poc.core.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Card entity operations.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, String> {
    
    /**
     * Find a card by its username (for customer login).
     */
    Optional<Card> findByUsername(String username);
}
