package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Utility for SHA-256 password hashing.
 *
 * Algorithm: SHA-256(salt + password) → lowercase hex string.
 * This is the same algorithm used by DataInitializer when creating sample users.
 */
public final class PasswordHasher {

    private PasswordHasher() {
        // utility class — not instantiable
    }

    /**
     * Produces a SHA-256 hex digest of (salt + plain).
     *
     * @param salt  per-user unique salt
     * @param plain plaintext password
     * @return lowercase hex string (64 characters)
     */
    public static String hash(String salt, String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((salt + plain).getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 hashing failed", e);
        }
    }
}
