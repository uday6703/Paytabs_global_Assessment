package com.bank.poc.core.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class for encrypting and decrypting sensitive card data.
 * Uses AES-256 encryption for card number storage security.
 * 
 * IMPORTANT: In production, use a proper key management system (KMS).
 */
@Component
public class CryptoUtil {

    // In production, these should come from a secure key management system
    // AES-256 requires exactly 32 bytes (256 bits) key
    private static final String SECRET_KEY = "01234567890123456789012345678901"; // Exactly 32 bytes for AES-256
    private static final String INIT_VECTOR = "0123456789012345"; // 16 bytes for IV
    
    private final SecretKeySpec secretKeySpec;
    private final IvParameterSpec ivParameterSpec;

    public CryptoUtil() {
        this.secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        this.ivParameterSpec = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encrypt a plain text string using AES-256.
     * Used for encrypting card numbers for storage.
     */
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    /**
     * Decrypt an encrypted string using AES-256.
     * Used for decrypting stored card numbers.
     */
    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }

    /**
     * Mask a card number for display purposes.
     * Shows only the last 4 digits.
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Mask card number showing first 4 and last 4 digits.
     * Format: 4123********2345
     */
    public static String maskCardNumberPartial(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "****";
        }
        return cardNumber.substring(0, 4) + "********" + cardNumber.substring(cardNumber.length() - 4);
    }
}
