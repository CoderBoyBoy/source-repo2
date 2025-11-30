package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for tag information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagInfo {
    private String name;
    private String commitId;
    private String message;
    private String tagger;
    private String taggerEmail;
    private long tagTime;
    private boolean isAnnotated;
}
