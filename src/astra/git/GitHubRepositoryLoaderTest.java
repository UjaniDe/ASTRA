package astra.git;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.eclipse.jgit.api.Git;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GitHubRepositoryLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void clonesRepositoryAndCleansUp() throws Exception {
        Path origin = tempDir.resolve("origin");
        Files.createDirectories(origin);

        try (Git git = Git.init().setDirectory(origin.toFile()).call()) {
            Files.writeString(origin.resolve("sample.toy"),
                "function sample() { validate(); }");

            git.add().addFilepattern("sample.toy").call();
            git.commit()
                .setMessage("Initial")
                .setAuthor("ASTRA Test", "test@example.com")
                .call();
        }

        Path clonePath;

        try (GitHubRepositoryLoader loader =
                new GitHubRepositoryLoader(origin.toUri().toString(), false)) {

            clonePath = Path.of(loader.getRepositoryPath());

            assertTrue(Files.isRegularFile(
                clonePath.resolve("sample.toy")
            ));
        }

        assertFalse(Files.exists(clonePath));
    }


    @Test
    void rejectsNonGitHubUrls() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new GitHubRepositoryLoader(
                "https://example.com/project.git"
            )
        );
    }

    @Test
    void rejectsLocalFileUrls() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new GitHubRepositoryLoader(
                "file:///tmp/example.git"
            )
        );
    }

    @Test
    void rejectsEmptyUrl() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new GitHubRepositoryLoader("")
        );
    }
}
