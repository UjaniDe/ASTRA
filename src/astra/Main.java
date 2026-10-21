package astra;

import astra.lexer.Lexer;
import astra.lexer.Token;
import astra.parser.Parser;
import astra.ast.WorkflowNode;
import astra.semantic.SemanticAnalyzer;
import astra.bytecode.BytecodeGenerator;
import astra.bytecode.Instruction;
import astra.vm.RuntimeEnvironment;
import astra.vm.VM;
import astra.git.GitHubRepositoryLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println(
                "Usage: java astra.Main <workflow.ast> <repository-path> "
                + "<source-file-path> [--run-tests] [--trace] "
                + "[--test-project <directory>]"
            );
            return;
        }

        boolean runTests = false;
        boolean trace = false;
        String testProject = ".";

        for (int i = 3; i < args.length; i++) {
            switch (args[i]) {
                case "--run-tests" -> runTests = true;
                case "--trace" -> trace = true;
                case "--test-project" -> {
                    if (i + 1 >= args.length) {
                        System.err.println(
                            "ASTRA ERROR: --test-project requires a directory"
                        );
                        return;
                    }
                    testProject = args[++i];
                }
                default -> {
                    System.err.println("Unknown option: " + args[i]);
                    return;
                }
            }
        }
        Path workflowPath = Path.of(args[0]);
        Path repositoryPath = Path.of(args[1]);

        if (!Files.isRegularFile(workflowPath)) {
            System.err.println(
                "ASTRA ERROR: Workflow file not found: " + workflowPath
            );
            System.exit(1);
        }

        boolean remoteRepository = args[1].startsWith("https://");

        if (!remoteRepository && !Files.isDirectory(repositoryPath)) {
            System.err.println(
                "ASTRA ERROR: Repository directory not found: " + repositoryPath
            );
            System.exit(1);
        }

        if (args[2].isBlank()) {
            System.err.println("ASTRA ERROR: Source file path cannot be empty.");
            System.exit(1);
        }

        String source = Files.readString(workflowPath);

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        System.out.println("=== TOKENS ===");
        for (Token t : tokens) System.out.println(t);

        Parser parser = new Parser(tokens);
        WorkflowNode ast;
        try {
            ast = parser.parse();
        } catch (RuntimeException e) {
            System.out.println("\n=== PARSE ERROR ===");
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("\n=== AST ===");
        System.out.print(ast.print(""));

        System.out.println("\n=== SEMANTIC ANALYSIS ===");
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(ast);

        System.out.println("\n-- Symbol Table --");
        System.out.print(analyzer.getSymbolTable().print());

        if (!analyzer.getWarnings().isEmpty()) {
            System.out.println("\n-- Warnings --");
            for (String w : analyzer.getWarnings()) System.out.println("WARNING: " + w);
        }

        if (!analyzer.getErrors().isEmpty()) {
            System.out.println("\n-- Errors --");
            for (String e : analyzer.getErrors()) System.out.println("ERROR: " + e);
            System.out.println("\nCompilation halted due to semantic errors — skipping bytecode generation.");
            return;
        }

        System.out.println("\n=== BYTECODE ===");
        BytecodeGenerator generator = new BytecodeGenerator();
        List<Instruction> bytecode = generator.generate(ast);
        System.out.print(BytecodeGenerator.print(bytecode));

        System.out.println("\n=== VM EXECUTION ===");
        RuntimeEnvironment env = new RuntimeEnvironment();

        env.setRepositoryPath(args[1]);
        env.setSourceFilePath(args[2]);

        env.setTraceEnabled(trace);

        if (runTests) {
            Path testProjectDir = Path.of(testProject)
                .toAbsolutePath().normalize();

            if (!Files.isDirectory(testProjectDir)) {
                System.err.println(
                    "ASTRA ERROR: Test project directory not found: "
                    + testProjectDir
                );
                System.exit(1);
            }

            if (!Files.isRegularFile(testProjectDir.resolve("pom.xml"))) {
                System.err.println(
                    "ASTRA ERROR: No pom.xml found in test project: "
                    + testProjectDir
                );
                System.exit(1);
            }

            env.setTestProjectPath(testProjectDir.toString());
            env.setTestExecutionEnabled(true);
        }

        try {
            if (remoteRepository) {
                try (GitHubRepositoryLoader loader =
                        new GitHubRepositoryLoader(args[1])) {

                    env.setRepositoryPath(loader.getRepositoryPath());

                    VM vm = new VM(bytecode, env);
                    vm.run();
                }
            } else {
                VM vm = new VM(bytecode, env);
                vm.run();
            }
        } catch (Exception e) {
            System.err.println(
                "ASTRA ERROR: " + e.getMessage()
            );
            System.exit(1);
        }
    }
}