package com.gitserver.repository;

import com.gitserver.model.SshKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for SshKey entity.
 */
@Repository
public interface SshKeyRepository extends JpaRepository<SshKey, Long> {
    
    List<SshKey> findByUsername(String username);
    
    Optional<SshKey> findByFingerprint(String fingerprint);
    
    boolean existsByFingerprint(String fingerprint);
}
