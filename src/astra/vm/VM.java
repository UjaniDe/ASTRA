package astra.vm;

import astra.bytecode.Instruction;
import astra.bytecode.OpCode;
import astra.git.GitComparisonService;
import astra.compare.StructuralChange;

import java.util.ArrayList;
import java.util.Collections;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class VM {
    private final List<Instruction> program;
    private final RuntimeEnvironment env;
    private final Deque<Object> stack = new ArrayDeque<>();
    private int ip = 0; // instruction pointer
    private List<StructuralChange> detectedChanges = new ArrayList<>();
    private String lastOldRevision;
    private String lastNewRevision;

    public List<StructuralChange> getDetectedChanges() {
        return Collections.unmodifiableList(detectedChanges);
    }

    public VM(List<Instruction> program, RuntimeEnvironment env) {
        this.program = program;
        this.env = env;
    }

    public void run() {
        while (ip < program.size()) {
            Instruction instr = program.get(ip);

            if (env.isTraceEnabled()) {
                System.out.printf("[TRACE] IP=%d  %s%n", ip, instr);
            }

            switch (instr.opcode) {
                case PUSH_STRING -> {
                    stack.push(instr.operand);
                    ip++;
                }
                case LOAD_COND -> {
                    boolean value = env.getCondition((String) instr.operand);
                    stack.push(value);
                    ip++;
                }
                case COMPARE -> {
                    String newRevision = (String) stack.pop();
                    String oldRevision = (String) stack.pop();

                    System.out.println("Comparing branch \"" + oldRevision
                        + "\" with \"" + newRevision + "\"");

                    lastOldRevision = oldRevision;
                    lastNewRevision = newRevision;

                    String repo = env.getRepositoryPath();
                    String file = env.getSourceFilePath();

                    if (repo == null || file == null) {
                        throw new IllegalStateException(
                            "Git comparison requires repository and source-file paths"
                        );
                    }

                    try {
                        GitComparisonService service =
                            new GitComparisonService(repo);

                        detectedChanges = new ArrayList<>(
                            service.compare(oldRevision, newRevision, file)
                        );

                        System.out.println("Structural changes detected: "
                            + detectedChanges.size());

                    } catch (Exception e) {
                        throw new RuntimeException(
                            "Git structural comparison failed: "
                            + e.getMessage(), e
                        );
                    }

                    ip++;
                }
                case ANALYZE_CHANGES -> {
                    System.out.println("=== STRUCTURAL ANALYSIS ===");

                    int total = detectedChanges.size();

                    long conditionChanges = detectedChanges.stream()
                        .filter(change ->
                            change.type == astra.compare.ChangeType.CONDITION_CHANGED)
                        .count();

                    boolean securityChange = conditionChanges > 0;

                    env.setCondition("security_change", securityChange);
                    env.setCondition("has_changes", total > 0);

                    System.out.println("Total structural changes: " + total);
                    System.out.println("Modified conditions: " + conditionChanges);
                    System.out.println("Security review flag: " + securityChange);

                    ip++;
                }
                case RUN_TESTS -> {
                    if (!env.isTestExecutionEnabled()) {
                        System.out.println(
                            "Running tests... (execution disabled)"
                        );
                        ip++;
                        break;
                    }

                    String projectPath = env.getTestProjectPath();

                    if (projectPath == null || projectPath.isBlank()) {
                        throw new IllegalStateException(
                            "Test project path must be configured"
                        );
                    }

                    System.out.println("=== ASTRA TEST EXECUTION ===");

                    try {
                        ProcessBuilder builder = new ProcessBuilder(
                            "mvn", "-q", "test"
                        );

                        builder.directory(
                            java.nio.file.Path.of(projectPath)
                                .toAbsolutePath().toFile()
                        );

                        builder.inheritIO();

                        Process process = builder.start();
                        int exitCode = process.waitFor();

                        System.out.println(
                            "Maven test exit code: " + exitCode
                        );

                        if (exitCode != 0) {
                            throw new RuntimeException(
                                "Automated tests failed with exit code "
                                + exitCode
                            );
                        }

                        System.out.println("Automated tests passed.");

                    } catch (java.io.IOException e) {
                        throw new RuntimeException(
                            "Failed to start Maven tests", e
                        );
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(
                            "Test execution interrupted", e
                        );
                    }

                    ip++;
                }
                case REPORT -> {
                    System.out.println("=== ASTRA REPORT ===");
                    System.out.println("Repository: " + env.getRepositoryPath());
                    System.out.println("File: " + env.getSourceFilePath());
                    System.out.println("Compared revisions: "
                        + lastOldRevision + " -> " + lastNewRevision);
                    System.out.println("Total structural changes: "
                        + detectedChanges.size());

                    if (detectedChanges.isEmpty()) {
                        System.out.println("No structural changes detected.");
                    } else {
                        System.out.println();
                        for (StructuralChange change : detectedChanges) {
                            System.out.print(change);
                            System.out.println();
                        }
                    }

                    ip++;
                }
                case JUMP -> ip = (Integer) instr.operand;
                case JUMP_IF_FALSE -> {
                    boolean cond = (Boolean) stack.pop();
                    if (!cond) {
                        ip = (Integer) instr.operand;
                    } else {
                        ip++;
                    }
                }
                case LABEL -> ip++; // no-op marker
                case HALT -> {
                    return;
                }
                default -> throw new RuntimeException("VM error: unknown opcode " + instr.opcode);
            }
        }
    }
}