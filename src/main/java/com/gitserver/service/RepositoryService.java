package com.gitserver.service;

import com.gitserver.config.GitServerConfig;
import com.gitserver.dto.*;
import com.gitserver.exception.GitOperationException;
import com.gitserver.exception.RepositoryNotFoundException;
import com.gitserver.model.Repository;
import com.gitserver.repository.RepositoryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Service for managing Git repositories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryService {

    private final GitServerConfig config;
    private final RepositoryJpaRepository repositoryJpaRepository;

    /**
     * Create a new repository.
     */
    @Transactional
    public RepositoryResponse createRepository(CreateRepositoryRequest request) {
        // Validate repository doesn't already exist
        if (repositoryJpaRepository.existsByOwnerAndName(request.getOwner(), request.getName())) {
            throw new IllegalArgumentException("Repository already exists: " + request.getOwner() + "/" + request.getName());
        }

        Path repoPath = config.getRepositoryPath(request.getOwner(), request.getName());

        try {
            // Create directory structure
            Files.createDirectories(repoPath.getParent());

            // Initialize bare Git repository
            Git.init()
                .setDirectory(repoPath.toFile())
                .setBare(true)
                .call();

            log.info("Created bare Git repository at: {}", repoPath);

            // Save repository metadata to database
            Repository repo = new Repository();
            repo.setName(request.getName());
            repo.setOwner(request.getOwner());
            repo.setDescription(request.getDescription());
            repo.setDefaultBranch(request.getDefaultBranch() != null ? request.getDefaultBranch() : "main");
            repo.setPrivate(request.isPrivate());

            repo = repositoryJpaRepository.save(repo);

            return toRepositoryResponse(repo);
        } catch (GitAPIException | IOException e) {
            throw new GitOperationException("Failed to create repository: " + e.getMessage(), e);
        }
    }

    /**
     * Get repository by owner and name.
     */
    public RepositoryResponse getRepository(String owner, String name) {
        Repository repo = repositoryJpaRepository.findByOwnerAndName(owner, name)
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found: " + owner + "/" + name));
        return toRepositoryResponse(repo);
    }

    /**
     * List repositories for an owner.
     */
    public List<RepositoryResponse> listRepositories(String owner) {
        return repositoryJpaRepository.findByOwner(owner).stream()
                .map(this::toRepositoryResponse)
                .toList();
    }

    /**
     * List all repositories.
     */
    public List<RepositoryResponse> listAllRepositories() {
        return repositoryJpaRepository.findAll().stream()
                .map(this::toRepositoryResponse)
                .toList();
    }

    /**
     * Delete a repository.
     */
    @Transactional
    public void deleteRepository(String owner, String name) {
        Repository repo = repositoryJpaRepository.findByOwnerAndName(owner, name)
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found: " + owner + "/" + name));

        Path repoPath = config.getRepositoryPath(owner, name);

        try {
            // Delete the Git repository files
            if (Files.exists(repoPath)) {
                try (Stream<Path> walk = Files.walk(repoPath)) {
                    walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.warn("Failed to delete file: {}", path, e);
                            }
                        });
                }
            }

            // Delete from database
            repositoryJpaRepository.delete(repo);

            log.info("Deleted repository: {}/{}", owner, name);
        } catch (IOException e) {
            throw new GitOperationException("Failed to delete repository: " + e.getMessage(), e);
        }
    }

    /**
     * Get the Git repository for a given owner and name.
     */
    public Git getGitRepository(String owner, String name) {
        // Verify repository exists in database
        if (!repositoryJpaRepository.existsByOwnerAndName(owner, name)) {
            throw new RepositoryNotFoundException("Repository not found: " + owner + "/" + name);
        }

        Path repoPath = config.getRepositoryPath(owner, name);
        if (!Files.exists(repoPath)) {
            throw new RepositoryNotFoundException("Repository files not found: " + owner + "/" + name);
        }

        try {
            return Git.open(repoPath.toFile());
        } catch (IOException e) {
            throw new GitOperationException("Failed to open repository: " + e.getMessage(), e);
        }
    }

    private RepositoryResponse toRepositoryResponse(Repository repo) {
        return RepositoryResponse.builder()
                .id(repo.getId())
                .name(repo.getName())
                .owner(repo.getOwner())
                .description(repo.getDescription())
                .defaultBranch(repo.getDefaultBranch())
                .isPrivate(repo.isPrivate())
                .cloneUrl("http://localhost:8080/git/" + repo.getOwner() + "/" + repo.getName() + ".git")
                .sshUrl("git@localhost:" + repo.getOwner() + "/" + repo.getName() + ".git")
                .createdAt(repo.getCreatedAt())
                .updatedAt(repo.getUpdatedAt())
                .build();
    }
}
