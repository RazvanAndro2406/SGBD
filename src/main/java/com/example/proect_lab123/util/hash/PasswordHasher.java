package com.example.proect_lab123.util.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher {

    private static final int SALT_LENGTH = 16; // 16 bytes for salt

    /**
     * Generează un hash al parolei folosind un salt nou.
     * Output-ul va include hash-ul și salt-ul, separate prin '$' (ex: "hash$salt").
     */
    public static String hashPassword(String rawPassword) {
        byte[] salt = generateSalt();
        String saltString = Base64.getEncoder().encodeToString(salt);

        String hashedPassword = hash(rawPassword, salt);

        return hashedPassword + "$" + saltString;
    }

    /**
     * Verifică o parolă brută (rawPassword) cu hash-ul stocat (care conține salt-ul).
     * Hash-ul stocat trebuie să fie în formatul "hash$salt".
     */
    public static boolean checkPassword(String rawPassword, String storedHashAndSalt) {
        if (storedHashAndSalt == null || !storedHashAndSalt.contains("$")) {
            return false;
        }

        String[] parts = storedHashAndSalt.split("\\$");
        String storedHash = parts[0];
        String saltString = parts[1];

        byte[] salt = Base64.getDecoder().decode(saltString);

        String rawPasswordHash = hash(rawPassword, salt);

        return storedHash.equals(rawPasswordHash);
    }

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    private static String hash(String password, byte[] salt) {
        try {
            // Folosim SHA-512 (o alegere robustă)
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // Conversie în String Base64 pentru stocare sigură
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Eroare la algoritmul de hashing: " + e.getMessage());
        }
    }
}