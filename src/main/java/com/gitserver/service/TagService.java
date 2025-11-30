package com.gitserver.service;

import com.gitserver.dto.CreateTagRequest;
import com.gitserver.dto.TagInfo;
import com.gitserver.exception.GitOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing Git tags.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final RepositoryService repositoryService;

    /**
     * List all tags in a repository.
     */
    public List<TagInfo> listTags(String owner, String repoName) {
        try (Git git = repositoryService.getGitRepository(owner, repoName)) {
            Repository repository = git.getRepository();
            List<TagInfo> tags = new ArrayList<>();

            List<Ref> refs = git.tagList().call();
            for (Ref ref : refs) {
                TagInfo tagInfo = createTagInfo(repository, ref);
                if (tagInfo != null) {
                    tags.add(tagInfo);
                }
            }

            return tags;
        } catch (GitAPIException e) {
            throw new GitOperationException("Failed to list tags: " + e.getMessage(), e);
        }
    }

    /**
     * Get information about a specific tag.
     */
    public TagInfo getTag(String owner, String repoName, String tagName) {
        try (Git git = repositoryService.getGitRepository(owner, repoName)) {
            Repository repository = git.getRepository();

            Ref ref = repository.findRef("refs/tags/" + tagName);
            if (ref == null) {
                throw new GitOperationException("Tag not found: " + tagName);
            }

            TagInfo tagInfo = createTagInfo(repository, ref);
            if (tagInfo == null) {
                throw new GitOperationException("Failed to parse tag: " + tagName);
            }

            return tagInfo;
        } catch (IOException e) {
            throw new GitOperationException("Failed to get tag: " + e.getMessage(), e);
        }
    }

    /**
     * Create a new tag.
     */
    public TagInfo createTag(String owner, String repoName, CreateTagRequest request) {
        try (Git git = repositoryService.getGitRepository(owner, repoName)) {
            Repository repository = git.getRepository();

            // Find the commit to tag
            ObjectId commitId;
            if (request.getCommitId() != null && !request.getCommitId().isEmpty()) {
                commitId = repository.resolve(request.getCommitId());
                if (commitId == null) {
                    throw new GitOperationException("Commit not found: " + request.getCommitId());
                }
            } else {
                // Use HEAD if no commit specified
                commitId = repository.resolve("HEAD");
                if (commitId == null) {
                    throw new GitOperationException("Repository has no commits");
                }
            }

            Ref tagRef;
            if (request.isAnnotated()) {
                // Create annotated tag
                tagRef = git.tag()
                        .setName(request.getTagName())
                        .setMessage(request.getMessage())
                        .setObjectId(new RevWalk(repository).parseCommit(commitId))
                        .call();
            } else {
                // Create lightweight tag
                tagRef = git.tag()
                        .setName(request.getTagName())
                        .setAnnotated(false)
                        .setObjectId(new RevWalk(repository).parseCommit(commitId))
                        .call();
            }

            log.info("Created tag '{}' at commit {} in {}/{}", 
                    request.getTagName(), commitId.getName(), owner, repoName);

            return createTagInfo(repository, tagRef);
        } catch (GitAPIException | IOException e) {
            throw new GitOperationException("Failed to create tag: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a tag.
     */
    public void deleteTag(String owner, String repoName, String tagName) {
        try (Git git = repositoryService.getGitRepository(owner, repoName)) {
            Repository repository = git.getRepository();

            Ref ref = repository.findRef("refs/tags/" + tagName);
            if (ref == null) {
                throw new GitOperationException("Tag not found: " + tagName);
            }

            git.tagDelete()
                    .setTags(tagName)
                    .call();

            log.info("Deleted tag '{}' from {}/{}", tagName, owner, repoName);
        } catch (GitAPIException | IOException e) {
            throw new GitOperationException("Failed to delete tag: " + e.getMessage(), e);
        }
    }

    private TagInfo createTagInfo(Repository repository, Ref ref) {
        String tagName = ref.getName();
        if (tagName.startsWith("refs/tags/")) {
            tagName = tagName.substring("refs/tags/".length());
        }

        try (RevWalk revWalk = new RevWalk(repository)) {
            ObjectId objectId = ref.getPeeledObjectId();
            if (objectId == null) {
                objectId = ref.getObjectId();
            }

            if (objectId == null) {
                return null;
            }

            RevObject revObject = revWalk.parseAny(ref.getObjectId());

            if (revObject instanceof RevTag revTag) {
                // Annotated tag
                RevCommit commit = revWalk.parseCommit(revTag.getObject());
                return TagInfo.builder()
                        .name(tagName)
                        .commitId(commit.getName())
                        .message(revTag.getFullMessage())
                        .tagger(revTag.getTaggerIdent() != null ? revTag.getTaggerIdent().getName() : null)
                        .taggerEmail(revTag.getTaggerIdent() != null ? revTag.getTaggerIdent().getEmailAddress() : null)
                        .tagTime(revTag.getTaggerIdent() != null ? revTag.getTaggerIdent().getWhen().getTime() : 0)
                        .isAnnotated(true)
                        .build();
            } else if (revObject instanceof RevCommit commit) {
                // Lightweight tag
                return TagInfo.builder()
                        .name(tagName)
                        .commitId(commit.getName())
                        .message(null)
                        .tagger(null)
                        .taggerEmail(null)
                        .tagTime(commit.getCommitTime() * 1000L)
                        .isAnnotated(false)
                        .build();
            }

            return null;
        } catch (IOException e) {
            log.warn("Failed to parse tag {}: {}", tagName, e.getMessage());
            return null;
        }
    }
}
