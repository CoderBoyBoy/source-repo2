package com.gitserver.controller;

import com.gitserver.dto.CreateTagRequest;
import com.gitserver.dto.TagInfo;
import com.gitserver.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for tag management.
 */
@RestController
@RequestMapping("/api/repos/{owner}/{repo}/tags")
@RequiredArgsConstructor
@Tag(name = "Tag Management", description = "APIs for managing Git tags")
public class TagController {

    private final TagService tagService;

    @GetMapping
    @Operation(summary = "List all tags", description = "Returns a list of all tags in the repository")
    public ResponseEntity<List<TagInfo>> listTags(
            @Parameter(description = "Repository owner") @PathVariable String owner,
            @Parameter(description = "Repository name") @PathVariable String repo) {
        List<TagInfo> tags = tagService.listTags(owner, repo);
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/{tag}")
    @Operation(summary = "Get tag details", description = "Returns detailed information about a specific tag")
    public ResponseEntity<TagInfo> getTag(
            @Parameter(description = "Repository owner") @PathVariable String owner,
            @Parameter(description = "Repository name") @PathVariable String repo,
            @Parameter(description = "Tag name") @PathVariable String tag) {
        TagInfo tagInfo = tagService.getTag(owner, repo, tag);
        return ResponseEntity.ok(tagInfo);
    }

    @PostMapping
    @Operation(summary = "Create a new tag", description = "Creates a new tag for a specific commit")
    public ResponseEntity<TagInfo> createTag(
            @Parameter(description = "Repository owner") @PathVariable String owner,
            @Parameter(description = "Repository name") @PathVariable String repo,
            @RequestBody CreateTagRequest request) {
        TagInfo tagInfo = tagService.createTag(owner, repo, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tagInfo);
    }

    @DeleteMapping("/{tag}")
    @Operation(summary = "Delete a tag", description = "Deletes a tag from the repository")
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "Repository owner") @PathVariable String owner,
            @Parameter(description = "Repository name") @PathVariable String repo,
            @Parameter(description = "Tag name") @PathVariable String tag) {
        tagService.deleteTag(owner, repo, tag);
        return ResponseEntity.noContent().build();
    }
}
