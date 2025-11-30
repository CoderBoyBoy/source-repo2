package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * DTO for repository response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryResponse {
    private Long id;
    private String name;
    private String owner;
    private String description;
    private String defaultBranch;
    private boolean isPrivate;
    private String cloneUrl;
    private String sshUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
