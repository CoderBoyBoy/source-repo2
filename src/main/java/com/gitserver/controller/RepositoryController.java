package com.gitserver.controller;

import com.gitserver.dto.CreateRepositoryRequest;
import com.gitserver.dto.RepositoryResponse;
import com.gitserver.service.RepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for repository management.
 */
@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
@Tag(name = "Repository Management", description = "APIs for managing Git repositories")
public class RepositoryController {

    private final RepositoryService repositoryService;

    @PostMapping
    @Operation(summary = "Create a new repository", description = "Creates a new Git repository with the specified configuration")
    public ResponseEntity<RepositoryResponse> createRepository(@RequestBody CreateRepositoryRequest request) {
        RepositoryResponse response = repositoryService.createRepository(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all repositories", description = "Returns a list of all repositories")
    public ResponseEntity<List<RepositoryResponse>> listAllRepositories() {
        List<RepositoryResponse> repositories = repositoryService.listAllRepositories();
        return ResponseEntity.ok(repositories);
    }

    @GetMapping("/owner/{owner}")
    @Operation(summary = "List repositories by owner", description = "Returns a list of repositories for the specified owner")
    public ResponseEntity<List<RepositoryResponse>> listRepositoriesByOwner(
            @Parameter(description = "Repository owner") @PathVariable String owner) {
        List<RepositoryResponse> repositories = repositoryService.listRepositories(owner);
        return ResponseEntity.ok(repositories);
    }

    @GetMapping("/{owner}/{name}")
    @Operation(summary = "Get repository details", description = "Returns detailed information about a specific repository")
    public ResponseEntity<RepositoryResponse> getRepository(
            @Parameter(description = "Repository owner") @PathVariable String owner,
            @Parameter(description = "Repository name") @PathVariable String name) {
        RepositoryResponse response = repositoryService.getRepository(owner, name);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{owner}/{name}")
    @Operation(summary = "Delete a repository", description = "Deletes a repository and all its data")
    public ResponseEntity<Void> deleteRepository(
            @Parameter(description = "Repository owner") @PathVariable String owner,
            @Parameter(description = "Repository name") @PathVariable String name) {
        repositoryService.deleteRepository(owner, name);
        return ResponseEntity.noContent().build();
    }
}
