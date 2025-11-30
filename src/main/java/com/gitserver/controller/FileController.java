package com.gitserver.controller;

import com.gitserver.dto.CommitInfo;
import com.gitserver.dto.FileContent;
import com.gitserver.dto.TreeEntry;
import com.gitserver.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for file and directory browsing.
 */
@RestController
@RequestMapping("/api/repos/{owner}/{repo}")
@RequiredArgsConstructor
@Tag(name = "File Browsing", description = "APIs for browsing repository files and directories")
public class FileController {

    private final FileService fileService;

    @GetMapping("/tree/{ref}")
    @Operation(summary = "Get directory tree", description = "Returns the file tree at a specific ref (branch, tag, or commit)")
    public ResponseEntity<List<TreeEntry>> getTree(
            @Parameter(description = "Repository owner") @PathVariable String owner,
            @Parameter(description = "Repository name") @PathVariable String repo,
            @Parameter(description = "Git ref (branch, tag, or commit SHA)") @PathVariable String ref,
            @Parameter(description = "Directory path (optional)") @RequestParam(required = false) String path) {
        List<TreeEntry> entries = fileService.getTree(owner, repo, ref, path);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/contents/{ref}/**")
    @Operation(summary = "Get file content", description = "Returns the content of a file at a specific ref")
    public ResponseEntity<FileContent> getFileContent(
            @Parameter(description = "Repository owner") @PathVariable String owner,
            @Parameter(description = "Repository name") @PathVariable String repo,
            @Parameter(description = "Git ref (branch, tag, or commit SHA)") @PathVariable String ref,
            jakarta.servlet.http.HttpServletRequest request) {
        // Extract the file path from the request URI
        String requestUri = request.getRequestURI();
        String prefix = "/api/repos/" + owner + "/" + repo + "/contents/" + ref + "/";
        String filePath = requestUri.substring(prefix.length());
        
        FileContent content = fileService.getFileContent(owner, repo, ref, filePath);
        return ResponseEntity.ok(content);
    }

    @GetMapping("/commits/{ref}")
    @Operation(summary = "Get commit history", description = "Returns the commit history for a specific ref")
    public ResponseEntity<List<CommitInfo>> getCommits(
            @Parameter(description = "Repository owner") @PathVariable String owner,
            @Parameter(description = "Repository name") @PathVariable String repo,
            @Parameter(description = "Git ref (branch, tag, or commit SHA)") @PathVariable String ref,
            @Parameter(description = "Maximum number of commits to return") @RequestParam(defaultValue = "30") int limit) {
        List<CommitInfo> commits = fileService.getCommits(owner, repo, ref, limit);
        return ResponseEntity.ok(commits);
    }

    @GetMapping("/commit/{sha}")
    @Operation(summary = "Get commit details", description = "Returns detailed information about a specific commit")
    public ResponseEntity<CommitInfo> getCommit(
            @Parameter(description = "Repository owner") @PathVariable String owner,
            @Parameter(description = "Repository name") @PathVariable String repo,
            @Parameter(description = "Commit SHA") @PathVariable String sha) {
        CommitInfo commit = fileService.getCommit(owner, repo, sha);
        return ResponseEntity.ok(commit);
    }
}
