package com.gitserver.controller;

import com.gitserver.dto.AddSshKeyRequest;
import com.gitserver.dto.SshKeyResponse;
import com.gitserver.service.SshKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for SSH key management.
 */
@RestController
@RequestMapping("/api/users/{username}/ssh-keys")
@RequiredArgsConstructor
@Tag(name = "SSH Key Management", description = "APIs for managing SSH keys")
public class SshKeyController {

    private final SshKeyService sshKeyService;

    @GetMapping
    @Operation(summary = "List SSH keys", description = "Returns a list of all SSH keys for a user")
    public ResponseEntity<List<SshKeyResponse>> listSshKeys(
            @Parameter(description = "Username") @PathVariable String username) {
        List<SshKeyResponse> keys = sshKeyService.listSshKeys(username);
        return ResponseEntity.ok(keys);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get SSH key details", description = "Returns detailed information about a specific SSH key")
    public ResponseEntity<SshKeyResponse> getSshKey(
            @Parameter(description = "Username") @PathVariable String username,
            @Parameter(description = "SSH key ID") @PathVariable Long id) {
        SshKeyResponse key = sshKeyService.getSshKey(id);
        return ResponseEntity.ok(key);
    }

    @PostMapping
    @Operation(summary = "Add a new SSH key", description = "Adds a new SSH public key for the user")
    public ResponseEntity<SshKeyResponse> addSshKey(
            @Parameter(description = "Username") @PathVariable String username,
            @RequestBody AddSshKeyRequest request) {
        request.setUsername(username);
        SshKeyResponse key = sshKeyService.addSshKey(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(key);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an SSH key", description = "Deletes an SSH key for the user")
    public ResponseEntity<Void> deleteSshKey(
            @Parameter(description = "Username") @PathVariable String username,
            @Parameter(description = "SSH key ID") @PathVariable Long id) {
        sshKeyService.deleteSshKeyForUser(username, id);
        return ResponseEntity.noContent().build();
    }
}
