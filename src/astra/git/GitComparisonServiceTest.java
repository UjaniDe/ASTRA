package astra.git;

import astra.compare.ChangeType;
import astra.compare.StructuralChange;

import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GitComparisonServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void comparesAllModifiedToyFiles() throws Exception {
        Path accessFile = tempDir.resolve("access.toy");
        Path loggingFile = tempDir.resolve("logging.toy");
        Path stableFile = tempDir.resolve("stable.toy");

        try (Git git = Git.init()
                .setDirectory(tempDir.toFile()).call()) {

            Files.writeString(accessFile, """
                function grantAccess(user) {
                    if (user.isAdmin) {
                        logAccess(user);
                    }
                }
                """);

            Files.writeString(loggingFile, """
                function record(user) {
                    logAccess(user);
                }
                """);

            Files.writeString(stableFile, """
                function stable() {
                    validate();
                }
                """);

            git.add().addFilepattern(".").call();

            String oldRevision = git.commit()
                .setMessage("Version A")
                .setAuthor("ASTRA Test", "test@example.com")
                .call().getName();

            Files.writeString(accessFile, """
                function grantAccess(user) {
                    if (user.isAdmin || user.isGuest) {
                        logAccess(user);
                    }
                }
                """);

            Files.writeString(loggingFile, """
                function record(user) {
                    logAccess(user, "granted");
                }
                """);

            git.add().addFilepattern(".").call();

            String newRevision = git.commit()
                .setMessage("Version B")
                .setAuthor("ASTRA Test", "test@example.com")
                .call().getName();

            GitComparisonService service =
                new GitComparisonService(tempDir.toString());

            Map<String, List<StructuralChange>> results =
                service.compareChangedToyFiles(
                    oldRevision, newRevision
                );

            assertEquals(2, results.size());

            assertTrue(results.containsKey("access.toy"));
            assertTrue(results.containsKey("logging.toy"));
            assertFalse(results.containsKey("stable.toy"));

            assertTrue(results.get("access.toy").stream()
                .anyMatch(change ->
                    change.type == ChangeType.CONDITION_CHANGED));

            assertTrue(results.get("logging.toy").stream()
                .anyMatch(change ->
                    change.type == ChangeType.ARGUMENTS_CHANGED));
        }
    }
}
