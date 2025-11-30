package com.gitserver.repository;

import com.gitserver.model.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Repository entity.
 */
public interface RepositoryJpaRepository extends JpaRepository<Repository, Long> {
    
    Optional<Repository> findByOwnerAndName(String owner, String name);
    
    List<Repository> findByOwner(String owner);
    
    boolean existsByOwnerAndName(String owner, String name);
}
