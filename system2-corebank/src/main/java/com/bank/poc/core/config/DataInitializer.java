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

import java.time.LocalDateTime;

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
        // Check if data already exists (for production with persistent database)
        if (cardRepository.count() > 0) {
            log.info("Database already has data. Skipping initialization.");
            return;
        }

        log.info("Initializing database with sample data...");

        // Create Card 1 - John Doe (Customer 1)
        Card card1 = new Card();
        card1.setCardNumber("4123456789012345");
        card1.setCardNumberEncrypted(cryptoUtil.encrypt("4123456789012345"));
        card1.setPinHash(cardService.hashPin("1234"));  // PIN: 1234 (stored as SHA-256 hash)
        card1.setBalance(5250.75);
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
        card2.setBalance(12500.00);
        card2.setCustomerName("Jane Smith");
        card2.setUsername("cust2");
        card2.setActive(true);
        cardRepository.save(card2);
        log.info("Created card for Jane Smith (username: cust2, PIN: ****)");

        // ==================== Sample Transactions for John Doe ====================
        // Initial deposit
        Transaction tx1 = new Transaction();
        tx1.setCardNumber("4123456789012345");
        tx1.setType("topup");
        tx1.setAmount(5000.00);
        tx1.setStatus("SUCCESS");
        tx1.setReason("Initial deposit - Account opening");
        transactionRepository.save(tx1);

        // Salary deposit
        Transaction tx2 = new Transaction();
        tx2.setCardNumber("4123456789012345");
        tx2.setType("topup");
        tx2.setAmount(3500.00);
        tx2.setStatus("SUCCESS");
        tx2.setReason("Salary deposit - November 2025");
        transactionRepository.save(tx2);

        // Grocery shopping
        Transaction tx3 = new Transaction();
        tx3.setCardNumber("4123456789012345");
        tx3.setType("withdraw");
        tx3.setAmount(150.25);
        tx3.setStatus("SUCCESS");
        tx3.setReason("Grocery shopping - SuperMart");
        transactionRepository.save(tx3);

        // Utility bill
        Transaction tx4 = new Transaction();
        tx4.setCardNumber("4123456789012345");
        tx4.setType("withdraw");
        tx4.setAmount(200.00);
        tx4.setStatus("SUCCESS");
        tx4.setReason("Electricity bill payment");
        transactionRepository.save(tx4);

        // Coffee shop
        Transaction tx5 = new Transaction();
        tx5.setCardNumber("4123456789012345");
        tx5.setType("withdraw");
        tx5.setAmount(25.50);
        tx5.setStatus("SUCCESS");
        tx5.setReason("Coffee Shop - Morning brew");
        transactionRepository.save(tx5);

        // Online shopping
        Transaction tx6 = new Transaction();
        tx6.setCardNumber("4123456789012345");
        tx6.setType("withdraw");
        tx6.setAmount(350.00);
        tx6.setStatus("SUCCESS");
        tx6.setReason("Online purchase - Electronics");
        transactionRepository.save(tx6);

        // Bonus deposit
        Transaction tx7 = new Transaction();
        tx7.setCardNumber("4123456789012345");
        tx7.setType("topup");
        tx7.setAmount(1000.00);
        tx7.setStatus("SUCCESS");
        tx7.setReason("Performance bonus");
        transactionRepository.save(tx7);

        // Restaurant
        Transaction tx8 = new Transaction();
        tx8.setCardNumber("4123456789012345");
        tx8.setType("withdraw");
        tx8.setAmount(85.00);
        tx8.setStatus("SUCCESS");
        tx8.setReason("Restaurant - Family dinner");
        transactionRepository.save(tx8);

        // Failed transaction (insufficient balance attempt - simulation)
        Transaction tx9 = new Transaction();
        tx9.setCardNumber("4123456789012345");
        tx9.setType("withdraw");
        tx9.setAmount(50000.00);
        tx9.setStatus("FAILED");
        tx9.setReason("Insufficient balance");
        transactionRepository.save(tx9);

        // ATM withdrawal
        Transaction tx10 = new Transaction();
        tx10.setCardNumber("4123456789012345");
        tx10.setType("withdraw");
        tx10.setAmount(500.00);
        tx10.setStatus("SUCCESS");
        tx10.setReason("ATM withdrawal");
        transactionRepository.save(tx10);

        // ==================== Sample Transactions for Jane Smith ====================
        // Initial deposit
        Transaction tx11 = new Transaction();
        tx11.setCardNumber("4987654321098765");
        tx11.setType("topup");
        tx11.setAmount(10000.00);
        tx11.setStatus("SUCCESS");
        tx11.setReason("Initial deposit - Account opening");
        transactionRepository.save(tx11);

        // Salary deposit
        Transaction tx12 = new Transaction();
        tx12.setCardNumber("4987654321098765");
        tx12.setType("topup");
        tx12.setAmount(5500.00);
        tx12.setStatus("SUCCESS");
        tx12.setReason("Salary deposit - November 2025");
        transactionRepository.save(tx12);

        // Rent payment
        Transaction tx13 = new Transaction();
        tx13.setCardNumber("4987654321098765");
        tx13.setType("withdraw");
        tx13.setAmount(1500.00);
        tx13.setStatus("SUCCESS");
        tx13.setReason("Rent payment - December");
        transactionRepository.save(tx13);

        // Shopping
        Transaction tx14 = new Transaction();
        tx14.setCardNumber("4987654321098765");
        tx14.setType("withdraw");
        tx14.setAmount(450.00);
        tx14.setStatus("SUCCESS");
        tx14.setReason("Shopping mall - Clothing");
        transactionRepository.save(tx14);

        // Insurance payment
        Transaction tx15 = new Transaction();
        tx15.setCardNumber("4987654321098765");
        tx15.setType("withdraw");
        tx15.setAmount(300.00);
        tx15.setStatus("SUCCESS");
        tx15.setReason("Monthly insurance premium");
        transactionRepository.save(tx15);

        // Gym membership
        Transaction tx16 = new Transaction();
        tx16.setCardNumber("4987654321098765");
        tx16.setType("withdraw");
        tx16.setAmount(75.00);
        tx16.setStatus("SUCCESS");
        tx16.setReason("Gym membership - Monthly");
        transactionRepository.save(tx16);

        // Freelance income
        Transaction tx17 = new Transaction();
        tx17.setCardNumber("4987654321098765");
        tx17.setType("topup");
        tx17.setAmount(1200.00);
        tx17.setStatus("SUCCESS");
        tx17.setReason("Freelance project payment");
        transactionRepository.save(tx17);

        // Medical expense
        Transaction tx18 = new Transaction();
        tx18.setCardNumber("4987654321098765");
        tx18.setType("withdraw");
        tx18.setAmount(125.00);
        tx18.setStatus("SUCCESS");
        tx18.setReason("Pharmacy - Medical supplies");
        transactionRepository.save(tx18);

        // Investment
        Transaction tx19 = new Transaction();
        tx19.setCardNumber("4987654321098765");
        tx19.setType("withdraw");
        tx19.setAmount(2000.00);
        tx19.setStatus("SUCCESS");
        tx19.setReason("Monthly investment transfer");
        transactionRepository.save(tx19);

        // Refund
        Transaction tx20 = new Transaction();
        tx20.setCardNumber("4987654321098765");
        tx20.setType("topup");
        tx20.setAmount(250.00);
        tx20.setStatus("SUCCESS");
        tx20.setReason("Product return refund");
        transactionRepository.save(tx20);

        log.info("Database initialized with {} cards and {} transactions!", 
                cardRepository.count(), transactionRepository.count());
        log.info("=================================================");
        log.info("TEST CREDENTIALS (for testing only):");
        log.info("Customer 1: username=cust1, password=pass, PIN=1234, Balance=$5,250.75");
        log.info("Customer 2: username=cust2, password=pass, PIN=5678, Balance=$12,500.00");
        log.info("Super Admin: username=admin, password=admin");
        log.info("=================================================");
        log.info("SECURITY NOTES:");
        log.info("- All PINs are stored as SHA-256 hashes");
        log.info("- Card numbers are encrypted with AES-256");
        log.info("- Plain text PINs are NEVER logged or stored");
        log.info("=================================================");
    }
}
