package com.gitserver.service;

import com.gitserver.config.GitServerConfig;
import com.gitserver.dto.CreateRepositoryRequest;
import com.gitserver.dto.RepositoryResponse;
import com.gitserver.repository.RepositoryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "git.server.repositories.base-path=${java.io.tmpdir}/test-repos",
    "git.server.ssh-keys.base-path=${java.io.tmpdir}/test-ssh-keys"
})
class RepositoryServiceTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RepositoryJpaRepository repositoryJpaRepository;

    @BeforeEach
    void setUp() {
        repositoryJpaRepository.deleteAll();
    }

    @Test
    void testCreateRepository() {
        CreateRepositoryRequest request = new CreateRepositoryRequest();
        request.setName("test-repo");
        request.setOwner("testuser");
        request.setDescription("A test repository");
        request.setDefaultBranch("main");
        request.setPrivate(false);

        RepositoryResponse response = repositoryService.createRepository(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("test-repo", response.getName());
        assertEquals("testuser", response.getOwner());
        assertEquals("A test repository", response.getDescription());
        assertEquals("main", response.getDefaultBranch());
        assertFalse(response.isPrivate());
        assertNotNull(response.getCloneUrl());
        assertNotNull(response.getSshUrl());
    }

    @Test
    void testListRepositories() {
        // Create two repositories
        CreateRepositoryRequest request1 = new CreateRepositoryRequest();
        request1.setName("repo1");
        request1.setOwner("testuser");
        repositoryService.createRepository(request1);

        CreateRepositoryRequest request2 = new CreateRepositoryRequest();
        request2.setName("repo2");
        request2.setOwner("testuser");
        repositoryService.createRepository(request2);

        List<RepositoryResponse> repos = repositoryService.listRepositories("testuser");

        assertEquals(2, repos.size());
    }

    @Test
    void testGetRepository() {
        CreateRepositoryRequest request = new CreateRepositoryRequest();
        request.setName("get-test-repo");
        request.setOwner("testuser");
        repositoryService.createRepository(request);

        RepositoryResponse response = repositoryService.getRepository("testuser", "get-test-repo");

        assertNotNull(response);
        assertEquals("get-test-repo", response.getName());
        assertEquals("testuser", response.getOwner());
    }

    @Test
    void testDeleteRepository() {
        CreateRepositoryRequest request = new CreateRepositoryRequest();
        request.setName("delete-test-repo");
        request.setOwner("testuser");
        repositoryService.createRepository(request);

        repositoryService.deleteRepository("testuser", "delete-test-repo");

        List<RepositoryResponse> repos = repositoryService.listRepositories("testuser");
        assertTrue(repos.isEmpty());
    }
}
