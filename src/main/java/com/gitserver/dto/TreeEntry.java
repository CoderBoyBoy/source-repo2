package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

/**
 * DTO for file/directory tree entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreeEntry {
    private String name;
    private String path;
    private String type; // "file" or "directory"
    private String mode;
    private String sha;
    private long size;
    private List<TreeEntry> children;
}
