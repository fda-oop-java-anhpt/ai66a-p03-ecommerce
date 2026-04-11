package com.oop.project.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordUtils {

    /**
     * Hashes a plain text password using SHA-256.
     * 
     * @param plainTextPassword The plain text password to hash.
     * @return The hashed password, encoded in Base64 (or Hex).
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(plainTextPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verifies if the provided plain text password matches the hashed password.
     * 
     * @param plainTextPassword The plain text password to verify.
     * @param hashedPassword The hashed password stored in the database.
     * @return True if passwords match, false otherwise.
     */
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) return false;
        String hashedInput = hashPassword(plainTextPassword);
        return hashedInput.equals(hashedPassword);
    }
}
