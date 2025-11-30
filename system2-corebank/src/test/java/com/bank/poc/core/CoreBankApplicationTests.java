package com.bank.poc.core;

import com.bank.poc.core.dto.TransactionRequest;
import com.bank.poc.core.dto.TransactionResponse;
import com.bank.poc.core.entity.Card;
import com.bank.poc.core.repository.CardRepository;
import com.bank.poc.core.service.CardService;
import com.bank.poc.core.util.CryptoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test cases for the Core Banking System (System 2).
 * 
 * Test Scenarios:
 * 1. Successful withdrawal with valid card/PIN
 * 2. Successful top-up with valid card/PIN
 * 3. Decline for invalid card
 * 4. Decline for invalid PIN
 * 5. Decline for insufficient balance (withdrawal)
 * 6. PIN hashing verification
 * 7. Card encryption verification
 */
@SpringBootTest
@Transactional
class CoreBankApplicationTests {

    @Autowired
    private CardService cardService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CryptoUtil cryptoUtil;

    private static final String VALID_CARD_NUMBER = "4111111111111111";
    private static final String VALID_PIN = "1234";
    private static final double INITIAL_BALANCE = 1000.00;

    @BeforeEach
    void setUp() {
        // Create a test card with encrypted data
        Card testCard = new Card();
        testCard.setCardNumber(VALID_CARD_NUMBER);
        testCard.setCardNumberEncrypted(cryptoUtil.encrypt(VALID_CARD_NUMBER));
        testCard.setPinHash(cardService.hashPin(VALID_PIN));
        testCard.setBalance(INITIAL_BALANCE);
        testCard.setCustomerName("Test User");
        testCard.setUsername("testuser");
        testCard.setActive(true);
        cardRepository.save(testCard);
    }

    @Nested
    @DisplayName("Successful Transaction Tests")
    class SuccessfulTransactionTests {

        @Test
        @DisplayName("TC001: Successful withdrawal with valid card and PIN")
        void testSuccessfulWithdrawal() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber(VALID_CARD_NUMBER);
            request.setPin(VALID_PIN);
            request.setAmount(100.00);
            request.setType("withdraw");

            // Act
            TransactionResponse response = cardService.processTransaction(request);

            // Assert
            assertTrue(response.isSuccess(), "Withdrawal should be successful");
            assertEquals("Withdrawal successful", response.getMessage());
            assertEquals(900.00, response.getNewBalance(), 0.01);
            assertNotNull(response.getTransactionId());
        }

        @Test
        @DisplayName("TC002: Successful top-up with valid card and PIN")
        void testSuccessfulTopUp() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber(VALID_CARD_NUMBER);
            request.setPin(VALID_PIN);
            request.setAmount(500.00);
            request.setType("topup");

            // Act
            TransactionResponse response = cardService.processTransaction(request);

            // Assert
            assertTrue(response.isSuccess(), "Top-up should be successful");
            assertEquals("Top-up successful", response.getMessage());
            assertEquals(1500.00, response.getNewBalance(), 0.01);
            assertNotNull(response.getTransactionId());
        }

        @Test
        @DisplayName("TC003: Multiple transactions update balance correctly")
        void testMultipleTransactions() {
            // First: Top-up
            TransactionRequest topupRequest = new TransactionRequest();
            topupRequest.setCardNumber(VALID_CARD_NUMBER);
            topupRequest.setPin(VALID_PIN);
            topupRequest.setAmount(200.00);
            topupRequest.setType("topup");
            
            TransactionResponse topupResponse = cardService.processTransaction(topupRequest);
            assertTrue(topupResponse.isSuccess());
            assertEquals(1200.00, topupResponse.getNewBalance(), 0.01);

            // Second: Withdrawal
            TransactionRequest withdrawRequest = new TransactionRequest();
            withdrawRequest.setCardNumber(VALID_CARD_NUMBER);
            withdrawRequest.setPin(VALID_PIN);
            withdrawRequest.setAmount(300.00);
            withdrawRequest.setType("withdraw");

            TransactionResponse withdrawResponse = cardService.processTransaction(withdrawRequest);
            assertTrue(withdrawResponse.isSuccess());
            assertEquals(900.00, withdrawResponse.getNewBalance(), 0.01);
        }
    }

    @Nested
    @DisplayName("Failed Transaction Tests")
    class FailedTransactionTests {

        @Test
        @DisplayName("TC004: Decline for invalid card number")
        void testInvalidCard() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber("4999999999999999"); // Non-existent card
            request.setPin(VALID_PIN);
            request.setAmount(100.00);
            request.setType("withdraw");

            // Act
            TransactionResponse response = cardService.processTransaction(request);

            // Assert
            assertFalse(response.isSuccess(), "Transaction should fail for invalid card");
            assertEquals("Invalid card", response.getMessage());
            assertNull(response.getNewBalance());
        }

        @Test
        @DisplayName("TC005: Decline for invalid PIN")
        void testInvalidPin() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber(VALID_CARD_NUMBER);
            request.setPin("9999"); // Wrong PIN
            request.setAmount(100.00);
            request.setType("withdraw");

            // Act
            TransactionResponse response = cardService.processTransaction(request);

            // Assert
            assertFalse(response.isSuccess(), "Transaction should fail for invalid PIN");
            assertEquals("Invalid PIN", response.getMessage());
            assertNull(response.getNewBalance());
        }

        @Test
        @DisplayName("TC006: Decline for insufficient balance on withdrawal")
        void testInsufficientBalance() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber(VALID_CARD_NUMBER);
            request.setPin(VALID_PIN);
            request.setAmount(5000.00); // More than balance
            request.setType("withdraw");

            // Act
            TransactionResponse response = cardService.processTransaction(request);

            // Assert
            assertFalse(response.isSuccess(), "Transaction should fail for insufficient balance");
            assertEquals("Insufficient balance", response.getMessage());
            assertNull(response.getNewBalance());
        }

        @Test
        @DisplayName("TC007: Decline for inactive card")
        void testInactiveCard() {
            // Arrange - Deactivate the card
            Card card = cardRepository.findById(VALID_CARD_NUMBER).orElseThrow();
            card.setActive(false);
            cardRepository.save(card);

            TransactionRequest request = new TransactionRequest();
            request.setCardNumber(VALID_CARD_NUMBER);
            request.setPin(VALID_PIN);
            request.setAmount(100.00);
            request.setType("withdraw");

            // Act
            TransactionResponse response = cardService.processTransaction(request);

            // Assert
            assertFalse(response.isSuccess(), "Transaction should fail for inactive card");
            assertEquals("Card is inactive", response.getMessage());
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("TC008: PIN is stored as SHA-256 hash")
        void testPinHashing() {
            // Arrange
            String plainPin = "1234";
            
            // Act
            String hashedPin = cardService.hashPin(plainPin);
            
            // Assert
            assertNotEquals(plainPin, hashedPin, "PIN should not be stored in plain text");
            assertEquals(64, hashedPin.length(), "SHA-256 hash should be 64 characters");
            
            // Verify same input produces same hash
            String hashedPin2 = cardService.hashPin(plainPin);
            assertEquals(hashedPin, hashedPin2, "Same PIN should produce same hash");
            
            // Verify different PIN produces different hash
            String differentHash = cardService.hashPin("5678");
            assertNotEquals(hashedPin, differentHash, "Different PINs should produce different hashes");
        }

        @Test
        @DisplayName("TC009: PIN verification works correctly")
        void testPinVerification() {
            // Arrange
            String correctPin = "1234";
            String wrongPin = "5678";
            String storedHash = cardService.hashPin(correctPin);

            // Act & Assert
            assertTrue(cardService.verifyPin(correctPin, storedHash), 
                "Correct PIN should verify successfully");
            assertFalse(cardService.verifyPin(wrongPin, storedHash), 
                "Wrong PIN should fail verification");
        }

        @Test
        @DisplayName("TC010: Card number encryption and decryption")
        void testCardNumberEncryption() {
            // Arrange
            String originalCardNumber = "4123456789012345";
            
            // Act
            String encrypted = cryptoUtil.encrypt(originalCardNumber);
            String decrypted = cryptoUtil.decrypt(encrypted);
            
            // Assert
            assertNotEquals(originalCardNumber, encrypted, 
                "Encrypted card number should differ from original");
            assertEquals(originalCardNumber, decrypted, 
                "Decrypted card number should match original");
        }

        @Test
        @DisplayName("TC011: Card number masking")
        void testCardNumberMasking() {
            // Arrange
            String cardNumber = "4123456789012345";
            
            // Act
            String masked = CryptoUtil.maskCardNumber(cardNumber);
            String partialMasked = CryptoUtil.maskCardNumberPartial(cardNumber);
            
            // Assert
            assertEquals("****2345", masked, "Should show only last 4 digits");
            assertEquals("4123********2345", partialMasked, "Should show first 4 and last 4 digits");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("TC012: Withdrawal of exact balance")
        void testWithdrawExactBalance() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber(VALID_CARD_NUMBER);
            request.setPin(VALID_PIN);
            request.setAmount(INITIAL_BALANCE); // Exactly the balance
            request.setType("withdraw");

            // Act
            TransactionResponse response = cardService.processTransaction(request);

            // Assert
            assertTrue(response.isSuccess(), "Should allow withdrawal of exact balance");
            assertEquals(0.00, response.getNewBalance(), 0.01);
        }

        @Test
        @DisplayName("TC013: Small amount transaction")
        void testSmallAmountTransaction() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber(VALID_CARD_NUMBER);
            request.setPin(VALID_PIN);
            request.setAmount(0.01); // Minimum amount
            request.setType("topup");

            // Act
            TransactionResponse response = cardService.processTransaction(request);

            // Assert
            assertTrue(response.isSuccess(), "Should handle small amounts");
            assertEquals(1000.01, response.getNewBalance(), 0.01);
        }

        @Test
        @DisplayName("TC014: Large amount transaction")
        void testLargeAmountTransaction() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber(VALID_CARD_NUMBER);
            request.setPin(VALID_PIN);
            request.setAmount(1000000.00); // Large amount
            request.setType("topup");

            // Act
            TransactionResponse response = cardService.processTransaction(request);

            // Assert
            assertTrue(response.isSuccess(), "Should handle large amounts");
            assertEquals(1001000.00, response.getNewBalance(), 0.01);
        }
    }
}
