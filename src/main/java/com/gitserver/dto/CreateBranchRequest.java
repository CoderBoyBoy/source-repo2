package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for creating a new branch.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBranchRequest {
    private String branchName;
    private String sourceBranch;
}
