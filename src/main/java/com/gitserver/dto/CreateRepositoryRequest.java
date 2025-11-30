package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for creating a new repository.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRepositoryRequest {
    private String name;
    private String owner;
    private String description;
    private String defaultBranch = "main";
    private boolean isPrivate = false;
}
