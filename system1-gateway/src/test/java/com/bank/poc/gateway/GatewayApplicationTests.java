package com.bank.poc.gateway;

import com.bank.poc.gateway.controller.TransactionController;
import com.bank.poc.gateway.dto.TransactionRequest;
import com.bank.poc.gateway.dto.TransactionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for System 1 (Gateway API).
 * 
 * Test Scenarios:
 * 1. Card range routing (only cards starting with '4')
 * 2. Input validation (cardNumber, pin, amount, type)
 * 3. Unsupported card range decline
 */
@SpringBootTest
class GatewayApplicationTests {

    @Autowired
    private TransactionController transactionController;

    @Nested
    @DisplayName("Card Range Routing Tests")
    class CardRangeRoutingTests {

        @Test
        @DisplayName("TC-GW-001: Accept card starting with '4' (Visa simulation)")
        void testAcceptCardStartingWith4() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber("4123456789012345");
            request.setPin("1234");
            request.setAmount(100.00);
            request.setType("topup");

            // Act
            ResponseEntity<TransactionResponse> response = transactionController.handleTransaction(request);

            // Assert - Should not return a card range error
            // Note: This test may fail to connect to System 2, but should pass validation
            assertNotNull(response);
            TransactionResponse body = response.getBody();
            assertNotNull(body);
            // If connection fails, it's not a validation error
            if (!body.isSuccess()) {
                assertFalse(body.getMessage().contains("Card range not supported"),
                    "Card starting with 4 should not be rejected for card range");
            }
        }

        @Test
        @DisplayName("TC-GW-002: Decline card starting with '5' (Mastercard simulation)")
        void testDeclineCardStartingWith5() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber("5123456789012345");
            request.setPin("1234");
            request.setAmount(100.00);
            request.setType("topup");

            // Act
            ResponseEntity<TransactionResponse> response = transactionController.handleTransaction(request);

            // Assert
            assertNotNull(response);
            assertEquals(400, response.getStatusCode().value());
            TransactionResponse body = response.getBody();
            assertNotNull(body);
            assertFalse(body.isSuccess());
            assertTrue(body.getMessage().contains("Card range not supported"),
                "Card starting with 5 should be declined for unsupported card range");
        }

        @Test
        @DisplayName("TC-GW-003: Decline card starting with '3' (Amex simulation)")
        void testDeclineCardStartingWith3() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber("3782822463100050");
            request.setPin("1234");
            request.setAmount(100.00);
            request.setType("topup");

            // Act
            ResponseEntity<TransactionResponse> response = transactionController.handleTransaction(request);

            // Assert
            assertNotNull(response);
            TransactionResponse body = response.getBody();
            assertNotNull(body);
            assertFalse(body.isSuccess());
            assertTrue(body.getMessage().contains("Card range not supported"));
        }

        @Test
        @DisplayName("TC-GW-004: Decline card starting with '6'")
        void testDeclineCardStartingWith6() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber("6011111111111117");
            request.setPin("1234");
            request.setAmount(100.00);
            request.setType("topup");

            // Act
            ResponseEntity<TransactionResponse> response = transactionController.handleTransaction(request);

            // Assert
            TransactionResponse body = response.getBody();
            assertNotNull(body);
            assertFalse(body.isSuccess());
            assertTrue(body.getMessage().contains("Card range not supported"));
        }
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @DisplayName("TC-GW-005: Decline when card number is missing")
        void testMissingCardNumber() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber(null);
            request.setPin("1234");
            request.setAmount(100.00);
            request.setType("topup");

            // Act
            ResponseEntity<TransactionResponse> response = transactionController.handleTransaction(request);

            // Assert
            TransactionResponse body = response.getBody();
            assertNotNull(body);
            assertFalse(body.isSuccess());
            assertTrue(body.getMessage().contains("Card number is required"));
        }

        @Test
        @DisplayName("TC-GW-006: Decline when card number is empty")
        void testEmptyCardNumber() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber("");
            request.setPin("1234");
            request.setAmount(100.00);
            request.setType("topup");

            // Act
            ResponseEntity<TransactionResponse> response = transactionController.handleTransaction(request);

            // Assert
            TransactionResponse body = response.getBody();
            assertNotNull(body);
            assertFalse(body.isSuccess());
            assertTrue(body.getMessage().contains("Card number is required"));
        }

        @Test
        @DisplayName("TC-GW-007: Decline when PIN is missing")
        void testMissingPin() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber("4123456789012345");
            request.setPin(null);
            request.setAmount(100.00);
            request.setType("topup");

            // Act
            ResponseEntity<TransactionResponse> response = transactionController.handleTransaction(request);

            // Assert
            TransactionResponse body = response.getBody();
            assertNotNull(body);
            assertFalse(body.isSuccess());
            assertTrue(body.getMessage().contains("PIN is required"));
        }

        @Test
        @DisplayName("TC-GW-008: Decline when amount is zero")
        void testZeroAmount() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber("4123456789012345");
            request.setPin("1234");
            request.setAmount(0);
            request.setType("topup");

            // Act
            ResponseEntity<TransactionResponse> response = transactionController.handleTransaction(request);

            // Assert
            TransactionResponse body = response.getBody();
            assertNotNull(body);
            assertFalse(body.isSuccess());
            assertTrue(body.getMessage().contains("Amount must be greater than 0"));
        }

        @Test
        @DisplayName("TC-GW-009: Decline when amount is negative")
        void testNegativeAmount() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber("4123456789012345");
            request.setPin("1234");
            request.setAmount(-100.00);
            request.setType("topup");

            // Act
            ResponseEntity<TransactionResponse> response = transactionController.handleTransaction(request);

            // Assert
            TransactionResponse body = response.getBody();
            assertNotNull(body);
            assertFalse(body.isSuccess());
            assertTrue(body.getMessage().contains("Amount must be greater than 0"));
        }

        @Test
        @DisplayName("TC-GW-010: Decline when transaction type is missing")
        void testMissingType() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber("4123456789012345");
            request.setPin("1234");
            request.setAmount(100.00);
            request.setType(null);

            // Act
            ResponseEntity<TransactionResponse> response = transactionController.handleTransaction(request);

            // Assert
            TransactionResponse body = response.getBody();
            assertNotNull(body);
            assertFalse(body.isSuccess());
            assertTrue(body.getMessage().contains("Transaction type is required"));
        }

        @Test
        @DisplayName("TC-GW-011: Decline when transaction type is invalid")
        void testInvalidType() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber("4123456789012345");
            request.setPin("1234");
            request.setAmount(100.00);
            request.setType("transfer"); // Invalid type

            // Act
            ResponseEntity<TransactionResponse> response = transactionController.handleTransaction(request);

            // Assert
            TransactionResponse body = response.getBody();
            assertNotNull(body);
            assertFalse(body.isSuccess());
            assertTrue(body.getMessage().contains("Invalid transaction type"));
        }

        @Test
        @DisplayName("TC-GW-012: Accept valid withdraw type")
        void testValidWithdrawType() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber("4123456789012345");
            request.setPin("1234");
            request.setAmount(100.00);
            request.setType("withdraw");

            // Act
            ResponseEntity<TransactionResponse> response = transactionController.handleTransaction(request);

            // Assert - Should not fail due to type validation
            TransactionResponse body = response.getBody();
            assertNotNull(body);
            // If it fails, it should not be due to type validation
            if (!body.isSuccess()) {
                assertFalse(body.getMessage().contains("Invalid transaction type"),
                    "Withdraw type should be accepted");
            }
        }

        @Test
        @DisplayName("TC-GW-013: Accept valid topup type")
        void testValidTopupType() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber("4123456789012345");
            request.setPin("1234");
            request.setAmount(100.00);
            request.setType("topup");

            // Act
            ResponseEntity<TransactionResponse> response = transactionController.handleTransaction(request);

            // Assert - Should not fail due to type validation
            TransactionResponse body = response.getBody();
            assertNotNull(body);
            // If it fails, it should not be due to type validation
            if (!body.isSuccess()) {
                assertFalse(body.getMessage().contains("Invalid transaction type"),
                    "Topup type should be accepted");
            }
        }

        @Test
        @DisplayName("TC-GW-014: Decline invalid card number format (not 16 digits)")
        void testInvalidCardNumberFormat() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber("412345678901"); // Only 12 digits
            request.setPin("1234");
            request.setAmount(100.00);
            request.setType("topup");

            // Act
            ResponseEntity<TransactionResponse> response = transactionController.handleTransaction(request);

            // Assert
            TransactionResponse body = response.getBody();
            assertNotNull(body);
            assertFalse(body.isSuccess());
            assertTrue(body.getMessage().contains("Card number must be exactly 16 digits") ||
                      body.getMessage().contains("Card range not supported"));
        }

        @Test
        @DisplayName("TC-GW-015: Accept case-insensitive transaction type")
        void testCaseInsensitiveType() {
            // Arrange
            TransactionRequest request = new TransactionRequest();
            request.setCardNumber("4123456789012345");
            request.setPin("1234");
            request.setAmount(100.00);
            request.setType("TOPUP"); // Uppercase

            // Act
            ResponseEntity<TransactionResponse> response = transactionController.handleTransaction(request);

            // Assert - Should not fail due to type validation
            TransactionResponse body = response.getBody();
            assertNotNull(body);
            if (!body.isSuccess()) {
                assertFalse(body.getMessage().contains("Invalid transaction type"),
                    "Type should be case-insensitive");
            }
        }
    }
}
