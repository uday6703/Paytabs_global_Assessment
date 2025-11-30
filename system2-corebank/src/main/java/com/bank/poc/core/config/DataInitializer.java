package com.bank.poc.core.config;

import com.bank.poc.core.entity.Card;
import com.bank.poc.core.entity.Transaction;
import com.bank.poc.core.repository.CardRepository;
import com.bank.poc.core.repository.TransactionRepository;
import com.bank.poc.core.service.CardService;
import com.bank.poc.core.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data initializer to seed the database with sample data on startup.
 * Creates initial cards with hashed PINs and encrypted card numbers.
 * 
 * SECURITY: 
 * - PINs are hashed with SHA-256
 * - Card numbers are encrypted with AES-256
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final CardService cardService;
    private final CryptoUtil cryptoUtil;

    @Override
    public void run(String... args) {
        log.info("Initializing database with sample data...");

        // Create Card 1 - John Doe (Customer 1)
        Card card1 = new Card();
        card1.setCardNumber("4123456789012345");
        card1.setCardNumberEncrypted(cryptoUtil.encrypt("4123456789012345"));
        card1.setPinHash(cardService.hashPin("1234"));  // PIN: 1234 (stored as SHA-256 hash)
        card1.setBalance(1000.00);
        card1.setCustomerName("John Doe");
        card1.setUsername("cust1");
        card1.setActive(true);
        cardRepository.save(card1);
        log.info("Created card for John Doe (username: cust1, PIN: ****)");

        // Create Card 2 - Jane Smith (Customer 2)
        Card card2 = new Card();
        card2.setCardNumber("4987654321098765");
        card2.setCardNumberEncrypted(cryptoUtil.encrypt("4987654321098765"));
        card2.setPinHash(cardService.hashPin("5678"));  // PIN: 5678 (stored as SHA-256 hash)
        card2.setBalance(2500.00);
        card2.setCustomerName("Jane Smith");
        card2.setUsername("cust2");
        card2.setActive(true);
        cardRepository.save(card2);
        log.info("Created card for Jane Smith (username: cust2, PIN: ****)");

        // Create some sample transactions for demonstration
        Transaction tx1 = new Transaction();
        tx1.setCardNumber("4123456789012345");
        tx1.setType("topup");
        tx1.setAmount(500.00);
        tx1.setStatus("SUCCESS");
        tx1.setReason("Initial top-up");
        transactionRepository.save(tx1);

        Transaction tx2 = new Transaction();
        tx2.setCardNumber("4123456789012345");
        tx2.setType("withdraw");
        tx2.setAmount(100.00);
        tx2.setStatus("SUCCESS");
        tx2.setReason("ATM withdrawal");
        transactionRepository.save(tx2);

        Transaction tx3 = new Transaction();
        tx3.setCardNumber("4987654321098765");
        tx3.setType("topup");
        tx3.setAmount(1000.00);
        tx3.setStatus("SUCCESS");
        tx3.setReason("Salary deposit");
        transactionRepository.save(tx3);

        log.info("Database initialized successfully!");
        log.info("=================================================");
        log.info("TEST CREDENTIALS (for testing only):");
        log.info("Customer 1: username=cust1, password=pass, card=4123****2345, PIN=****");
        log.info("Customer 2: username=cust2, password=pass, card=4987****8765, PIN=****");
        log.info("Super Admin: username=admin, password=admin");
        log.info("=================================================");
        log.info("SECURITY NOTES:");
        log.info("- All PINs are stored as SHA-256 hashes");
        log.info("- Card numbers are encrypted with AES-256");
        log.info("- Plain text PINs are NEVER logged or stored");
        log.info("=================================================");
    }
}
