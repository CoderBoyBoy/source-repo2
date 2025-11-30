package com.gitserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration for Git Server including repository storage paths.
 */
@Configuration
public class GitServerConfig {

    @Value("${git.server.repositories.base-path}")
    private String repositoriesBasePath;

    @Value("${git.server.ssh-keys.base-path}")
    private String sshKeysBasePath;

    @PostConstruct
    public void init() throws IOException {
        // Create base directories if they don't exist
        Files.createDirectories(Paths.get(repositoriesBasePath));
        Files.createDirectories(Paths.get(sshKeysBasePath));
    }

    public String getRepositoriesBasePath() {
        return repositoriesBasePath;
    }

    public String getSshKeysBasePath() {
        return sshKeysBasePath;
    }

    public Path getRepositoryPath(String owner, String repoName) {
        return Paths.get(repositoriesBasePath, owner, repoName + ".git");
    }

    public Path getSshKeyPath(String username) {
        return Paths.get(sshKeysBasePath, username);
    }
}
