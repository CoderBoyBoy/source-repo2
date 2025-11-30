package com.gitserver.service;

import com.gitserver.dto.CommitInfo;
import com.gitserver.dto.FileContent;
import com.gitserver.dto.TreeEntry;
import com.gitserver.exception.GitOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service for browsing repository file structure.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final RepositoryService repositoryService;

    /**
     * Get the file tree for a repository at a specific ref.
     */
    public List<TreeEntry> getTree(String owner, String repoName, String ref, String path) {
        try (Git git = repositoryService.getGitRepository(owner, repoName)) {
            Repository repository = git.getRepository();

            // Resolve the ref to a commit
            ObjectId commitId = resolveRef(repository, ref);
            if (commitId == null) {
                throw new GitOperationException("Ref not found: " + ref);
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(commitId);
                RevTree tree = commit.getTree();

                return getTreeEntries(repository, tree, path);
            }
        } catch (IOException e) {
            throw new GitOperationException("Failed to get tree: " + e.getMessage(), e);
        }
    }

    /**
     * Get file content.
     */
    public FileContent getFileContent(String owner, String repoName, String ref, String path) {
        try (Git git = repositoryService.getGitRepository(owner, repoName)) {
            Repository repository = git.getRepository();

            // Resolve the ref to a commit
            ObjectId commitId = resolveRef(repository, ref);
            if (commitId == null) {
                throw new GitOperationException("Ref not found: " + ref);
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(commitId);
                RevTree tree = commit.getTree();

                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathFilter.create(path));

                    if (!treeWalk.next()) {
                        throw new GitOperationException("File not found: " + path);
                    }

                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);

                    byte[] bytes = loader.getBytes();
                    String content = new String(bytes, StandardCharsets.UTF_8);

                    return FileContent.builder()
                            .name(treeWalk.getNameString())
                            .path(path)
                            .sha(objectId.getName())
                            .size(loader.getSize())
                            .content(Base64.getEncoder().encodeToString(bytes))
                            .encoding("base64")
                            .type("file")
                            .build();
                }
            }
        } catch (IOException e) {
            throw new GitOperationException("Failed to get file content: " + e.getMessage(), e);
        }
    }

    /**
     * Get commit history.
     */
    public List<CommitInfo> getCommits(String owner, String repoName, String ref, int limit) {
        try (Git git = repositoryService.getGitRepository(owner, repoName)) {
            Repository repository = git.getRepository();

            ObjectId commitId = resolveRef(repository, ref);
            if (commitId == null) {
                throw new GitOperationException("Ref not found: " + ref);
            }

            List<CommitInfo> commits = new ArrayList<>();
            try (RevWalk revWalk = new RevWalk(repository)) {
                revWalk.markStart(revWalk.parseCommit(commitId));

                int count = 0;
                for (RevCommit commit : revWalk) {
                    if (count >= limit) {
                        break;
                    }

                    String[] parentShas = new String[commit.getParentCount()];
                    for (int i = 0; i < commit.getParentCount(); i++) {
                        parentShas[i] = commit.getParent(i).getName();
                    }

                    commits.add(CommitInfo.builder()
                            .sha(commit.getName())
                            .message(commit.getFullMessage())
                            .author(commit.getAuthorIdent().getName())
                            .authorEmail(commit.getAuthorIdent().getEmailAddress())
                            .authorTime(commit.getAuthorIdent().getWhen().getTime())
                            .committer(commit.getCommitterIdent().getName())
                            .committerEmail(commit.getCommitterIdent().getEmailAddress())
                            .committerTime(commit.getCommitterIdent().getWhen().getTime())
                            .parentShas(parentShas)
                            .build());

                    count++;
                }
            }

            return commits;
        } catch (IOException e) {
            throw new GitOperationException("Failed to get commits: " + e.getMessage(), e);
        }
    }

    /**
     * Get a specific commit.
     */
    public CommitInfo getCommit(String owner, String repoName, String sha) {
        try (Git git = repositoryService.getGitRepository(owner, repoName)) {
            Repository repository = git.getRepository();

            ObjectId commitId = repository.resolve(sha);
            if (commitId == null) {
                throw new GitOperationException("Commit not found: " + sha);
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(commitId);

                String[] parentShas = new String[commit.getParentCount()];
                for (int i = 0; i < commit.getParentCount(); i++) {
                    parentShas[i] = commit.getParent(i).getName();
                }

                return CommitInfo.builder()
                        .sha(commit.getName())
                        .message(commit.getFullMessage())
                        .author(commit.getAuthorIdent().getName())
                        .authorEmail(commit.getAuthorIdent().getEmailAddress())
                        .authorTime(commit.getAuthorIdent().getWhen().getTime())
                        .committer(commit.getCommitterIdent().getName())
                        .committerEmail(commit.getCommitterIdent().getEmailAddress())
                        .committerTime(commit.getCommitterIdent().getWhen().getTime())
                        .parentShas(parentShas)
                        .build();
            }
        } catch (IOException e) {
            throw new GitOperationException("Failed to get commit: " + e.getMessage(), e);
        }
    }

    private List<TreeEntry> getTreeEntries(Repository repository, RevTree tree, String path) throws IOException {
        List<TreeEntry> entries = new ArrayList<>();

        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(tree);
            treeWalk.setRecursive(false);

            if (path != null && !path.isEmpty()) {
                treeWalk.setFilter(PathFilter.create(path));
                // We need to enter the directory
                while (treeWalk.next()) {
                    if (treeWalk.getPathString().equals(path) && treeWalk.isSubtree()) {
                        treeWalk.enterSubtree();
                        break;
                    }
                }
            }

            while (treeWalk.next()) {
                String entryPath = treeWalk.getPathString();
                
                // If we have a path filter, only include direct children
                if (path != null && !path.isEmpty()) {
                    String relativePath = entryPath.substring(path.length());
                    if (relativePath.startsWith("/")) {
                        relativePath = relativePath.substring(1);
                    }
                    if (relativePath.contains("/")) {
                        continue;
                    }
                }

                ObjectId objectId = treeWalk.getObjectId(0);
                FileMode fileMode = treeWalk.getFileMode(0);

                String type = treeWalk.isSubtree() ? "directory" : "file";
                long size = 0;
                if (!treeWalk.isSubtree()) {
                    ObjectLoader loader = repository.open(objectId);
                    size = loader.getSize();
                }

                entries.add(TreeEntry.builder()
                        .name(treeWalk.getNameString())
                        .path(entryPath)
                        .type(type)
                        .mode(Integer.toOctalString(fileMode.getBits()))
                        .sha(objectId.getName())
                        .size(size)
                        .build());
            }
        }

        // Sort: directories first, then by name
        entries.sort((a, b) -> {
            if (a.getType().equals(b.getType())) {
                return a.getName().compareToIgnoreCase(b.getName());
            }
            return a.getType().equals("directory") ? -1 : 1;
        });

        return entries;
    }

    private ObjectId resolveRef(Repository repository, String ref) throws IOException {
        if (ref == null || ref.isEmpty()) {
            ref = "HEAD";
        }

        // Try to resolve as-is
        ObjectId objectId = repository.resolve(ref);
        if (objectId != null) {
            return objectId;
        }

        // Try as branch
        objectId = repository.resolve("refs/heads/" + ref);
        if (objectId != null) {
            return objectId;
        }

        // Try as tag
        objectId = repository.resolve("refs/tags/" + ref);
        return objectId;
    }
}
