package astra.git;

import org.eclipse.jgit.api.Git;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class GitHubRepositoryLoader implements AutoCloseable {

    private final Path cloneDirectory;

    public GitHubRepositoryLoader(String repositoryUrl) throws Exception {
        if (repositoryUrl == null ||
                !repositoryUrl.matches(
                    "https://github\\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+(?:\\.git)?/?")) {
            throw new IllegalArgumentException(
                "Expected a public GitHub HTTPS repository URL"
            );
        }

        cloneDirectory = Files.createTempDirectory("astra-github-");

        try (Git git = Git.cloneRepository()
                .setURI(repositoryUrl)
                .setDirectory(cloneDirectory.toFile())
                .setCloneAllBranches(true)
                .call()) {

            System.out.println("GitHub repository cloned successfully.");

        } catch (Exception e) {
            close();
            throw new IOException(
                "Failed to clone GitHub repository: " + e.getMessage(), e
            );
        }
    }

    public String getRepositoryPath() {
        return cloneDirectory.toString();
    }

    @Override
    public void close() throws IOException {
        if (!Files.exists(cloneDirectory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(cloneDirectory)) {
            for (Path path : paths.sorted(
                    Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
