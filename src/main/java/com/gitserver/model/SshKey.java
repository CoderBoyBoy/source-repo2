package com.gitserver.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entity representing an SSH key for a user.
 */
@Entity
@Table(name = "ssh_keys")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SshKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String title;

    @Column(name = "public_key", nullable = false, length = 4096)
    private String publicKey;

    @Column(name = "fingerprint")
    private String fingerprint;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
