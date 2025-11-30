package com.gitserver.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for adding an SSH key.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddSshKeyRequest {
    private String username;
    private String title;
    private String publicKey;
}
