package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for creating a new tag.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTagRequest {
    private String tagName;
    private String commitId;
    private String message;
    private boolean annotated = true;
}
