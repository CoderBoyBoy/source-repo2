package com.gitserver.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SshKeyUtilTest {

    // Sample RSA public key for testing
    private static final String VALID_RSA_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQC3Ef2z9N3WmwJW6KWNbzTqVE9 test@example.com";
    
    // Sample Ed25519 public key for testing  
    private static final String VALID_ED25519_KEY = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIOMqqnkVzrm0SdG6UOoqKLsabgH5C9okWi0dh2l9GKJl test@example.com";

    @Test
    void testIsValidPublicKey_withValidRsaKey() {
        assertTrue(SshKeyUtil.isValidPublicKey(VALID_RSA_KEY));
    }

    @Test
    void testIsValidPublicKey_withValidEd25519Key() {
        assertTrue(SshKeyUtil.isValidPublicKey(VALID_ED25519_KEY));
    }

    @Test
    void testIsValidPublicKey_withNullKey() {
        assertFalse(SshKeyUtil.isValidPublicKey(null));
    }

    @Test
    void testIsValidPublicKey_withEmptyKey() {
        assertFalse(SshKeyUtil.isValidPublicKey(""));
    }

    @Test
    void testIsValidPublicKey_withInvalidKeyType() {
        assertFalse(SshKeyUtil.isValidPublicKey("invalid-type AAAAB3NzaC1yc2E test@example.com"));
    }

    @Test
    void testIsValidPublicKey_withMissingKeyData() {
        assertFalse(SshKeyUtil.isValidPublicKey("ssh-rsa"));
    }

    @Test
    void testCalculateFingerprint_withValidKey() {
        // This should not throw an exception for a well-formed key
        String fingerprint = SshKeyUtil.calculateFingerprint(VALID_ED25519_KEY);
        assertNotNull(fingerprint);
        assertTrue(fingerprint.startsWith("SHA256:"));
    }
}
