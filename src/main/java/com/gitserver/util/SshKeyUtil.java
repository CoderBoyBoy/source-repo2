package com.gitserver.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for SSH key operations.
 */
public final class SshKeyUtil {

    private SshKeyUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Calculate the SHA256 fingerprint of an SSH public key.
     *
     * @param publicKey the public key string
     * @return the fingerprint in SHA256 format
     */
    public static String calculateFingerprint(String publicKey) {
        try {
            // Extract the key data (remove key type and comment)
            String[] parts = publicKey.trim().split("\\s+");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid SSH public key format");
            }
            
            byte[] keyBytes = Base64.getDecoder().decode(parts[1]);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(keyBytes);
            
            return "SHA256:" + Base64.getEncoder().encodeToString(hash).replace("=", "");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid SSH public key: " + e.getMessage(), e);
        }
    }

    /**
     * Validate SSH public key format.
     *
     * @param publicKey the public key string
     * @return true if valid, false otherwise
     */
    public static boolean isValidPublicKey(String publicKey) {
        if (publicKey == null || publicKey.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = publicKey.trim().split("\\s+");
        if (parts.length < 2) {
            return false;
        }
        
        String keyType = parts[0];
        return keyType.equals("ssh-rsa") || 
               keyType.equals("ssh-ed25519") || 
               keyType.equals("ssh-dss") ||
               keyType.equals("ecdsa-sha2-nistp256") ||
               keyType.equals("ecdsa-sha2-nistp384") ||
               keyType.equals("ecdsa-sha2-nistp521");
    }
}
