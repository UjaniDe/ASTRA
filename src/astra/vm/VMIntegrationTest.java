package astra.vm;

import astra.bytecode.Instruction;
import astra.bytecode.OpCode;
import astra.compare.ChangeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jgit.api.Git;

import static org.junit.jupiter.api.Assertions.*;

class VMIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void comparesRealGitRevisionsAndSetsConditions() throws Exception {
        Path file = tempDir.resolve("sample.toy");

        try (Git git = Git.init().setDirectory(tempDir.toFile()).call()) {
            Files.writeString(file, """
                function check(user) {
                    if (user.isAdmin) {
                        logAccess(user);
                    }
                }
                """);

            git.add().addFilepattern("sample.toy").call();
            git.commit().setMessage("Version A")
                .setAuthor("ASTRA Test", "test@example.com").call();

            var oldCommit = git.getRepository().resolve("HEAD").name();

            Files.writeString(file, """
                function check(user) {
                    if (user.isAdmin || user.isGuest) {
                        logAccess(user);
                    }
                }
                """);

            git.add().addFilepattern("sample.toy").call();
            git.commit().setMessage("Version B")
                .setAuthor("ASTRA Test", "test@example.com").call();

            var newCommit = git.getRepository().resolve("HEAD").name();

            RuntimeEnvironment env = new RuntimeEnvironment();
            env.setRepositoryPath(tempDir.toString());
            env.setSourceFilePath("sample.toy");

            List<Instruction> program = List.of(
                new Instruction(OpCode.PUSH_STRING, oldCommit),
                new Instruction(OpCode.PUSH_STRING, newCommit),
                new Instruction(OpCode.COMPARE, null),
                new Instruction(OpCode.ANALYZE_CHANGES, null),
                new Instruction(OpCode.HALT, null)
            );

            VM vm = new VM(program, env);
            vm.run();

            assertTrue(vm.getDetectedChanges().stream()
                .anyMatch(c -> c.type == ChangeType.CONDITION_CHANGED));

            assertTrue(env.getCondition("security_change"));
            assertTrue(env.getCondition("has_changes"));
        }
    }

    @Test
    void identicalRevisionsProduceNoChanges() throws Exception {
        Path file = tempDir.resolve("sample.toy");

        try (Git git = Git.init().setDirectory(tempDir.toFile()).call()) {
            Files.writeString(file, """
                function check(user) {
                    validate(user);
                }
                """);

            git.add().addFilepattern("sample.toy").call();
            git.commit().setMessage("Initial version")
                .setAuthor("ASTRA Test", "test@example.com").call();

            String revision = git.getRepository()
                .resolve("HEAD").name();

            RuntimeEnvironment env = new RuntimeEnvironment();
            env.setRepositoryPath(tempDir.toString());
            env.setSourceFilePath("sample.toy");

            List<Instruction> program = List.of(
                new Instruction(OpCode.PUSH_STRING, revision),
                new Instruction(OpCode.PUSH_STRING, revision),
                new Instruction(OpCode.COMPARE, null),
                new Instruction(OpCode.ANALYZE_CHANGES, null),
                new Instruction(OpCode.HALT, null)
            );

            VM vm = new VM(program, env);
            vm.run();

            assertEquals(0, vm.getDetectedChanges().size());
            assertFalse(env.getCondition("security_change"));
            assertFalse(env.getCondition("has_changes"));
        }
    }
    @Test
    void automaticallyComparesMultipleModifiedFiles() throws Exception {
        Path access = tempDir.resolve("access.toy");
        Path logging = tempDir.resolve("logging.toy");

        try (Git git = Git.init()
                .setDirectory(tempDir.toFile()).call()) {

            Files.writeString(access, """
                function grantAccess(user) {
                    if (user.isAdmin) {
                        logAccess(user);
                    }
                }
                """);

            Files.writeString(logging, """
                function record(user) {
                    logAccess(user);
                }
                """);

            git.add().addFilepattern(".").call();

            String oldRevision = git.commit()
                .setMessage("Version A")
                .setAuthor("ASTRA Test", "test@example.com")
                .call().getName();

            Files.writeString(access, """
                function grantAccess(user) {
                    if (user.isAdmin || user.isGuest) {
                        logAccess(user);
                    }
                }
                """);

            Files.writeString(logging, """
                function record(user) {
                    logAccess(user, "granted");
                }
                """);

            git.add().addFilepattern(".").call();

            String newRevision = git.commit()
                .setMessage("Version B")
                .setAuthor("ASTRA Test", "test@example.com")
                .call().getName();

            RuntimeEnvironment env = new RuntimeEnvironment();
            env.setRepositoryPath(tempDir.toString());
            env.setAllChangedEnabled(true);

            List<Instruction> program = List.of(
                new Instruction(OpCode.PUSH_STRING, oldRevision),
                new Instruction(OpCode.PUSH_STRING, newRevision),
                new Instruction(OpCode.COMPARE, null),
                new Instruction(OpCode.ANALYZE_CHANGES, null),
                new Instruction(OpCode.HALT, null)
            );

            VM vm = new VM(program, env);
            vm.run();

            assertEquals(2, vm.getDetectedChanges().size());

            assertTrue(vm.getDetectedChanges().stream()
                .anyMatch(c ->
                    c.type == ChangeType.CONDITION_CHANGED));

            assertTrue(vm.getDetectedChanges().stream()
                .anyMatch(c ->
                    c.type == ChangeType.ARGUMENTS_CHANGED));

            assertTrue(env.getCondition("security_change"));
            assertTrue(env.getCondition("has_changes"));
        }
    }

}
