package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for commit information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitInfo {
    private String sha;
    private String message;
    private String author;
    private String authorEmail;
    private long authorTime;
    private String committer;
    private String committerEmail;
    private long committerTime;
    private String[] parentShas;
}
