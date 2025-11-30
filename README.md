# Git Server - GitHub-like Backend Service

A GitHub-like backend service built with Spring Boot and JGit for managing Git repositories.

## Features

### Repository Management
- Create new Git repositories (bare repositories)
- List all repositories
- List repositories by owner
- Get repository details
- Delete repositories

### Branch Management
- List all branches in a repository
- Get branch details (commit info)
- Create new branches from existing branches
- Delete branches (except default branch)

### Tag Management
- List all tags in a repository
- Get tag details
- Create annotated or lightweight tags
- Delete tags

### SSH Key Management
- Add SSH public keys for users
- List SSH keys for a user
- Get SSH key details
- Delete SSH keys
- Automatic fingerprint calculation (SHA256)

### Repository File Browsing
- Browse directory tree at any ref (branch/tag/commit)
- Get file content (base64 encoded)
- View commit history
- Get commit details

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **JGit 6.8.0** - Pure Java Git implementation
- **H2 Database** - In-memory database for metadata
- **Lombok** - Reduce boilerplate code
- **SpringDoc OpenAPI** - API documentation

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Build and Run

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

The server will start on `http://localhost:8080`.

### API Documentation

Once the server is running, access the Swagger UI at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### H2 Console

Access the H2 database console at http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:gitserver`
- Username: `sa`
- Password: (empty)

## API Endpoints

### Repositories

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/repos` | Create a new repository |
| GET | `/api/repos` | List all repositories |
| GET | `/api/repos/owner/{owner}` | List repositories by owner |
| GET | `/api/repos/{owner}/{name}` | Get repository details |
| DELETE | `/api/repos/{owner}/{name}` | Delete a repository |

### Branches

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/repos/{owner}/{repo}/branches` | List all branches |
| GET | `/api/repos/{owner}/{repo}/branches/{branch}` | Get branch details |
| POST | `/api/repos/{owner}/{repo}/branches` | Create a new branch |
| DELETE | `/api/repos/{owner}/{repo}/branches/{branch}` | Delete a branch |

### Tags

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/repos/{owner}/{repo}/tags` | List all tags |
| GET | `/api/repos/{owner}/{repo}/tags/{tag}` | Get tag details |
| POST | `/api/repos/{owner}/{repo}/tags` | Create a new tag |
| DELETE | `/api/repos/{owner}/{repo}/tags/{tag}` | Delete a tag |

### SSH Keys

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/{username}/ssh-keys` | List SSH keys for user |
| GET | `/api/users/{username}/ssh-keys/{id}` | Get SSH key details |
| POST | `/api/users/{username}/ssh-keys` | Add a new SSH key |
| DELETE | `/api/users/{username}/ssh-keys/{id}` | Delete an SSH key |

### File Browsing

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/repos/{owner}/{repo}/tree/{ref}` | Get directory tree |
| GET | `/api/repos/{owner}/{repo}/contents/{ref}/**` | Get file content |
| GET | `/api/repos/{owner}/{repo}/commits/{ref}` | Get commit history |
| GET | `/api/repos/{owner}/{repo}/commit/{sha}` | Get commit details |

## Example Usage

### Create a Repository

```bash
curl -X POST http://localhost:8080/api/repos \
  -H "Content-Type: application/json" \
  -d '{
    "name": "my-repo",
    "owner": "john",
    "description": "My first repository",
    "defaultBranch": "main",
    "isPrivate": false
  }'
```

### Add an SSH Key

```bash
curl -X POST http://localhost:8080/api/users/john/ssh-keys \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Laptop",
    "publicKey": "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQ... user@laptop"
  }'
```

### List Branches

```bash
curl http://localhost:8080/api/repos/john/my-repo/branches
```

### Create a Tag

```bash
curl -X POST http://localhost:8080/api/repos/john/my-repo/tags \
  -H "Content-Type: application/json" \
  -d '{
    "tagName": "v1.0.0",
    "commitId": "abc123",
    "message": "Release version 1.0.0",
    "annotated": true
  }'
```

## Configuration

Application properties can be configured in `src/main/resources/application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | Server port |
| `git.server.repositories.base-path` | `${user.home}/git-repositories` | Base path for storing repositories |
| `git.server.ssh-keys.base-path` | `${user.home}/.ssh-keys` | Base path for SSH keys |

## Project Structure

```
src/main/java/com/gitserver/
├── GitServerApplication.java      # Main application class
├── config/
│   └── GitServerConfig.java       # Configuration class
├── controller/
│   ├── RepositoryController.java  # Repository APIs
│   ├── BranchController.java      # Branch APIs
│   ├── TagController.java         # Tag APIs
│   ├── SshKeyController.java      # SSH Key APIs
│   └── FileController.java        # File browsing APIs
├── service/
│   ├── RepositoryService.java     # Repository operations
│   ├── BranchService.java         # Branch operations
│   ├── TagService.java            # Tag operations
│   ├── SshKeyService.java         # SSH key operations
│   └── FileService.java           # File browsing operations
├── model/
│   ├── Repository.java            # Repository entity
│   └── SshKey.java                # SSH key entity
├── dto/
│   ├── *Request.java              # Request DTOs
│   ├── *Response.java             # Response DTOs
│   └── *Info.java                 # Info DTOs
├── repository/
│   ├── RepositoryJpaRepository.java
│   └── SshKeyRepository.java
├── exception/
│   ├── *Exception.java            # Custom exceptions
│   └── GlobalExceptionHandler.java
└── util/
    └── SshKeyUtil.java            # SSH key utilities
```

## License

This project is open source.
