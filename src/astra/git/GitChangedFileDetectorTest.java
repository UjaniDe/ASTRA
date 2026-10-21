package astra.git;

import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GitChangedFileDetectorTest {

    @TempDir
    Path tempDir;

    @Test
    void findsOnlyModifiedToyFiles() throws Exception {
        Path toyFile = tempDir.resolve("sample.toy");
        Path textFile = tempDir.resolve("notes.txt");
        Path unchangedFile = tempDir.resolve("unchanged.toy");

        try (Git git = Git.init()
                .setDirectory(tempDir.toFile()).call()) {

            Files.writeString(toyFile,
                "function check() { validate(); }");
            Files.writeString(textFile, "Version A");
            Files.writeString(unchangedFile,
                "function stable() { log(); }");

            git.add().addFilepattern(".").call();
            var oldCommit = git.commit()
                .setMessage("Version A")
                .setAuthor("ASTRA Test", "test@example.com")
                .call();

            Files.writeString(toyFile,
                "function check() { audit(); }");
            Files.writeString(textFile, "Version B");

            git.add().addFilepattern(".").call();
            var newCommit = git.commit()
                .setMessage("Version B")
                .setAuthor("ASTRA Test", "test@example.com")
                .call();

            GitChangedFileDetector detector =
                new GitChangedFileDetector();

            List<String> changed = detector.findChangedToyFiles(
                tempDir.toString(),
                oldCommit.getName(),
                newCommit.getName()
            );

            assertEquals(List.of("sample.toy"), changed);
        }
    }

    @Test
    void identicalRevisionsReturnNoFiles() throws Exception {
        try (Git git = Git.init()
                .setDirectory(tempDir.toFile()).call()) {

            Files.writeString(
                tempDir.resolve("sample.toy"),
                "function check() { validate(); }"
            );

            git.add().addFilepattern(".").call();
            var commit = git.commit()
                .setMessage("Initial")
                .setAuthor("ASTRA Test", "test@example.com")
                .call();

            GitChangedFileDetector detector =
                new GitChangedFileDetector();

            List<String> changed = detector.findChangedToyFiles(
                tempDir.toString(),
                commit.getName(),
                commit.getName()
            );

            assertTrue(changed.isEmpty());
        }
    }
}
