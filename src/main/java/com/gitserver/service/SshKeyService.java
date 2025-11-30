package com.gitserver.service;

import com.gitserver.dto.AddSshKeyRequest;
import com.gitserver.dto.SshKeyResponse;
import com.gitserver.exception.GitOperationException;
import com.gitserver.model.SshKey;
import com.gitserver.repository.SshKeyRepository;
import com.gitserver.util.SshKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing SSH keys.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SshKeyService {

    private final SshKeyRepository sshKeyRepository;

    /**
     * Add a new SSH key.
     */
    @Transactional
    public SshKeyResponse addSshKey(AddSshKeyRequest request) {
        // Validate the SSH key
        if (!SshKeyUtil.isValidPublicKey(request.getPublicKey())) {
            throw new IllegalArgumentException("Invalid SSH public key format");
        }

        // Calculate fingerprint
        String fingerprint = SshKeyUtil.calculateFingerprint(request.getPublicKey());

        // Check if key already exists
        if (sshKeyRepository.existsByFingerprint(fingerprint)) {
            throw new IllegalArgumentException("SSH key already exists");
        }

        // Save the key
        SshKey sshKey = new SshKey();
        sshKey.setUsername(request.getUsername());
        sshKey.setTitle(request.getTitle());
        sshKey.setPublicKey(request.getPublicKey());
        sshKey.setFingerprint(fingerprint);

        sshKey = sshKeyRepository.save(sshKey);

        log.info("Added SSH key '{}' for user '{}'", request.getTitle(), request.getUsername());

        return toSshKeyResponse(sshKey);
    }

    /**
     * List SSH keys for a user.
     */
    public List<SshKeyResponse> listSshKeys(String username) {
        return sshKeyRepository.findByUsername(username).stream()
                .map(this::toSshKeyResponse)
                .toList();
    }

    /**
     * Get SSH key by ID.
     */
    public SshKeyResponse getSshKey(Long id) {
        SshKey sshKey = sshKeyRepository.findById(id)
                .orElseThrow(() -> new GitOperationException("SSH key not found: " + id));
        return toSshKeyResponse(sshKey);
    }

    /**
     * Delete SSH key by ID.
     */
    @Transactional
    public void deleteSshKey(Long id) {
        SshKey sshKey = sshKeyRepository.findById(id)
                .orElseThrow(() -> new GitOperationException("SSH key not found: " + id));

        sshKeyRepository.delete(sshKey);

        log.info("Deleted SSH key '{}' for user '{}'", sshKey.getTitle(), sshKey.getUsername());
    }

    /**
     * Delete SSH key by ID for a specific user.
     */
    @Transactional
    public void deleteSshKeyForUser(String username, Long id) {
        SshKey sshKey = sshKeyRepository.findById(id)
                .orElseThrow(() -> new GitOperationException("SSH key not found: " + id));

        if (!sshKey.getUsername().equals(username)) {
            throw new IllegalArgumentException("SSH key does not belong to user: " + username);
        }

        sshKeyRepository.delete(sshKey);

        log.info("Deleted SSH key '{}' for user '{}'", sshKey.getTitle(), username);
    }

    private SshKeyResponse toSshKeyResponse(SshKey sshKey) {
        return SshKeyResponse.builder()
                .id(sshKey.getId())
                .username(sshKey.getUsername())
                .title(sshKey.getTitle())
                .fingerprint(sshKey.getFingerprint())
                .createdAt(sshKey.getCreatedAt())
                .build();
    }
}
