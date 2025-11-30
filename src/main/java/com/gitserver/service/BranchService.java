package com.gitserver.service;

import com.gitserver.dto.BranchInfo;
import com.gitserver.dto.CreateBranchRequest;
import com.gitserver.exception.BranchNotFoundException;
import com.gitserver.exception.GitOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing Git branches.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BranchService {

    private final RepositoryService repositoryService;

    /**
     * List all branches in a repository.
     */
    public List<BranchInfo> listBranches(String owner, String repoName) {
        try (Git git = repositoryService.getGitRepository(owner, repoName)) {
            Repository repository = git.getRepository();
            String defaultBranch = getDefaultBranch(repository);

            List<BranchInfo> branches = new ArrayList<>();
            List<Ref> refs = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.ALL)
                    .call();

            for (Ref ref : refs) {
                BranchInfo branchInfo = createBranchInfo(repository, ref, defaultBranch);
                if (branchInfo != null) {
                    branches.add(branchInfo);
                }
            }

            return branches;
        } catch (GitAPIException e) {
            throw new GitOperationException("Failed to list branches: " + e.getMessage(), e);
        }
    }

    /**
     * Get information about a specific branch.
     */
    public BranchInfo getBranch(String owner, String repoName, String branchName) {
        try (Git git = repositoryService.getGitRepository(owner, repoName)) {
            Repository repository = git.getRepository();
            String defaultBranch = getDefaultBranch(repository);

            Ref ref = repository.findRef("refs/heads/" + branchName);
            if (ref == null) {
                throw new BranchNotFoundException("Branch not found: " + branchName);
            }

            BranchInfo branchInfo = createBranchInfo(repository, ref, defaultBranch);
            if (branchInfo == null) {
                throw new BranchNotFoundException("Branch not found: " + branchName);
            }

            return branchInfo;
        } catch (IOException e) {
            throw new GitOperationException("Failed to get branch: " + e.getMessage(), e);
        }
    }

    /**
     * Create a new branch.
     */
    public BranchInfo createBranch(String owner, String repoName, CreateBranchRequest request) {
        try (Git git = repositoryService.getGitRepository(owner, repoName)) {
            Repository repository = git.getRepository();

            // Find the source branch or commit
            String sourceBranch = request.getSourceBranch();
            if (sourceBranch == null || sourceBranch.isEmpty()) {
                sourceBranch = getDefaultBranch(repository);
            }

            Ref sourceRef = repository.findRef("refs/heads/" + sourceBranch);
            if (sourceRef == null) {
                throw new BranchNotFoundException("Source branch not found: " + sourceBranch);
            }

            // Create the new branch
            Ref newBranch = git.branchCreate()
                    .setName(request.getBranchName())
                    .setStartPoint(sourceRef.getName())
                    .call();

            log.info("Created branch '{}' from '{}' in {}/{}", 
                    request.getBranchName(), sourceBranch, owner, repoName);

            return createBranchInfo(repository, newBranch, getDefaultBranch(repository));
        } catch (GitAPIException | IOException e) {
            throw new GitOperationException("Failed to create branch: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a branch.
     */
    public void deleteBranch(String owner, String repoName, String branchName) {
        try (Git git = repositoryService.getGitRepository(owner, repoName)) {
            Repository repository = git.getRepository();
            String defaultBranch = getDefaultBranch(repository);

            if (branchName.equals(defaultBranch)) {
                throw new IllegalArgumentException("Cannot delete the default branch: " + branchName);
            }

            Ref ref = repository.findRef("refs/heads/" + branchName);
            if (ref == null) {
                throw new BranchNotFoundException("Branch not found: " + branchName);
            }

            git.branchDelete()
                    .setBranchNames(branchName)
                    .setForce(true)
                    .call();

            log.info("Deleted branch '{}' from {}/{}", branchName, owner, repoName);
        } catch (GitAPIException | IOException e) {
            throw new GitOperationException("Failed to delete branch: " + e.getMessage(), e);
        }
    }

    private BranchInfo createBranchInfo(Repository repository, Ref ref, String defaultBranch) {
        String branchName = ref.getName();
        if (branchName.startsWith("refs/heads/")) {
            branchName = branchName.substring("refs/heads/".length());
        } else if (branchName.startsWith("refs/remotes/")) {
            branchName = branchName.substring("refs/remotes/".length());
        }

        try (RevWalk revWalk = new RevWalk(repository)) {
            ObjectId objectId = ref.getObjectId();
            if (objectId == null) {
                return null;
            }

            RevCommit commit = revWalk.parseCommit(objectId);

            return BranchInfo.builder()
                    .name(branchName)
                    .commitId(commit.getName())
                    .commitMessage(commit.getShortMessage())
                    .author(commit.getAuthorIdent().getName())
                    .authorEmail(commit.getAuthorIdent().getEmailAddress())
                    .commitTime(commit.getCommitTime() * 1000L)
                    .isDefault(branchName.equals(defaultBranch))
                    .build();
        } catch (IOException e) {
            log.warn("Failed to parse commit for branch {}: {}", branchName, e.getMessage());
            return null;
        }
    }

    private String getDefaultBranch(Repository repository) {
        try {
            String fullBranch = repository.getFullBranch();
            if (fullBranch != null && fullBranch.startsWith("refs/heads/")) {
                return fullBranch.substring("refs/heads/".length());
            }
            return "main";
        } catch (IOException e) {
            return "main";
        }
    }
}
