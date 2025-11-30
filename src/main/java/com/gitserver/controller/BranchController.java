package com.gitserver.controller;

import com.gitserver.dto.BranchInfo;
import com.gitserver.dto.CreateBranchRequest;
import com.gitserver.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for branch management.
 */
@RestController
@RequestMapping("/api/repos/{owner}/{repo}/branches")
@RequiredArgsConstructor
@Tag(name = "Branch Management", description = "APIs for managing Git branches")
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    @Operation(summary = "List all branches", description = "Returns a list of all branches in the repository")
    public ResponseEntity<List<BranchInfo>> listBranches(
            @Parameter(description = "Repository owner") @PathVariable String owner,
            @Parameter(description = "Repository name") @PathVariable String repo) {
        List<BranchInfo> branches = branchService.listBranches(owner, repo);
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/{branch}")
    @Operation(summary = "Get branch details", description = "Returns detailed information about a specific branch")
    public ResponseEntity<BranchInfo> getBranch(
            @Parameter(description = "Repository owner") @PathVariable String owner,
            @Parameter(description = "Repository name") @PathVariable String repo,
            @Parameter(description = "Branch name") @PathVariable String branch) {
        BranchInfo branchInfo = branchService.getBranch(owner, repo, branch);
        return ResponseEntity.ok(branchInfo);
    }

    @PostMapping
    @Operation(summary = "Create a new branch", description = "Creates a new branch from an existing branch")
    public ResponseEntity<BranchInfo> createBranch(
            @Parameter(description = "Repository owner") @PathVariable String owner,
            @Parameter(description = "Repository name") @PathVariable String repo,
            @RequestBody CreateBranchRequest request) {
        BranchInfo branchInfo = branchService.createBranch(owner, repo, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(branchInfo);
    }

    @DeleteMapping("/{branch}")
    @Operation(summary = "Delete a branch", description = "Deletes a branch from the repository")
    public ResponseEntity<Void> deleteBranch(
            @Parameter(description = "Repository owner") @PathVariable String owner,
            @Parameter(description = "Repository name") @PathVariable String repo,
            @Parameter(description = "Branch name") @PathVariable String branch) {
        branchService.deleteBranch(owner, repo, branch);
        return ResponseEntity.noContent().build();
    }
}
