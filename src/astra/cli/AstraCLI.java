package astra.cli;

import astra.Main;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class AstraCLI {

    private static final String VERSION = "1.0.0";

    public static void main(String[] args) throws Exception {
        try {
            execute(args);
        } catch (IllegalArgumentException e) {
            System.err.println("ASTRA ERROR: " + e.getMessage());
            System.exit(2);
        }
    }

    private static void execute(String[] args) throws Exception {
        if (args.length == 0 || args[0].equals("--help")
                || args[0].equals("help")) {
            help();
            return;
        }

        switch (args[0]) {
            case "--version", "version" ->
                System.out.println("ASTRA v" + VERSION);

            case "run" -> run(args);

            case "analyze" -> comparison(args, true);

            case "compare" -> comparison(args, false);

            case "demo" -> demo();

            case "test" -> test();

            default -> {
                System.err.println("Unknown command: " + args[0]);
                help();
                System.exit(2);
            }
        }
    }

    private static void help() {
        System.out.println("""

             █████╗ ███████╗████████╗██████╗  █████╗
            ██╔══██╗██╔════╝╚══██╔══╝██╔══██╗██╔══██╗
            ███████║███████╗   ██║   ██████╔╝███████║
            ██╔══██║╚════██║   ██║   ██╔══██╗██╔══██║
            ██║  ██║███████║   ██║   ██║  ██║██║  ██║
            ╚═╝  ╚═╝╚══════╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝

            Compiler-driven structural analysis engine

            USAGE
              astra <command> [options]

            COMMANDS
              analyze   Detect structural changes in modified .toy files
              compare   Compare a specific .toy file
              run       Execute a workflow DSL file
              demo      Run the local ASTRA demonstration
              test      Run the Maven test suite

            GENERAL
              astra --help
              astra --version

            EXAMPLES
              astra demo
              astra test
              astra analyze --repo . --from HEAD~1 --to HEAD
              astra compare --repo . --from HEAD~1 --to HEAD --file sample.toy
              astra run examples/github-demo.ast --repo . --all-changed
            """);
    }

    private static String option(String[] args, String key) {
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals(key)) {
                if (i + 1 >= args.length ||
                        args[i + 1].startsWith("--")) {
                    throw new IllegalArgumentException(
                        "Missing value for " + key
                    );
                }
                return args[i + 1];
            }
        }
        return null;
    }

    private static boolean flag(String[] args, String key) {
        for (String arg : args) {
            if (arg.equals(key)) return true;
        }
        return false;
    }

    private static String required(String[] args, String key) {
        String value = option(args, key);
        if (value == null) {
            throw new IllegalArgumentException(
                "Required option missing: " + key
            );
        }
        return value;
    }

    private static void comparison(String[] args, boolean allChanged)
            throws Exception {

        String repo = required(args, "--repo");
        String from = required(args, "--from");
        String to = required(args, "--to");

        String file = allChanged ? "--all-changed"
                                 : required(args, "--file");

        String workflow = """
            workflow cli_analysis {
                compare "%s" with "%s"
                analyze_changes
                if has_changes {
                    report
                } else {
                    report
                }
            }
            """.formatted(from, to);

        Path temporaryWorkflow =
            Files.createTempFile("astra-cli-", ".ast");

        try {
            Files.writeString(temporaryWorkflow, workflow);

            List<String> mainArgs = new ArrayList<>();
            mainArgs.add(temporaryWorkflow.toString());
            mainArgs.add(repo);
            mainArgs.add(file);

            if (flag(args, "--trace")) {
                mainArgs.add("--trace");
            }

            executeComparison(mainArgs, flag(args, "--trace"));

        } finally {
            Files.deleteIfExists(temporaryWorkflow);
        }
    }


    private static void executeComparison(
            List<String> mainArgs, boolean trace) throws Exception {

        if (trace) {
            Main.main(mainArgs.toArray(String[]::new));
            return;
        }

        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (PrintStream captured = new PrintStream(
                buffer, true, StandardCharsets.UTF_8)) {

            try {
                System.setOut(captured);
                Main.main(mainArgs.toArray(String[]::new));
            } finally {
                System.setOut(original);
            }
        } catch (Exception e) {
            original.print(buffer.toString(StandardCharsets.UTF_8));
            throw e;
        }

        String output = buffer.toString(StandardCharsets.UTF_8);
        int execution = output.indexOf("=== VM EXECUTION ===");

        if (execution < 0) {
            original.print(output);
            throw new IllegalStateException(
                "Compilation did not reach VM execution."
            );
        }

        original.println();
        original.println("ASTRA | Structural Analysis");
        original.println("===========================");
        original.print(output.substring(
            execution + "=== VM EXECUTION ===".length()
        ).stripLeading());
    }

    private static void run(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                "Usage: astra run <workflow.ast> --repo <path> "
                + "[--file <file> | --all-changed]"
            );
        }

        String workflow = args[1];
        String repo = required(args, "--repo");
        String file = flag(args, "--all-changed")
            ? "--all-changed"
            : required(args, "--file");

        List<String> mainArgs = new ArrayList<>();
        mainArgs.add(workflow);
        mainArgs.add(repo);
        mainArgs.add(file);

        if (flag(args, "--trace")) {
            mainArgs.add("--trace");
        }

        Main.main(mainArgs.toArray(String[]::new));
    }

    private static void demo() throws Exception {
        System.out.println();
        System.out.println("======================================");
        System.out.println("       ASTRA STRUCTURAL ANALYSIS      ");
        System.out.println("======================================");
        System.out.println("Compiler-driven Git change detection");
        System.out.println();
        System.out.println("Demo: Comparing two committed versions");
        System.out.println("Source: examples/toylang/github-demo.toy");
        System.out.println();

        String[] arguments = {
            "analyze",
            "--repo", ".",
            "--from", "ec45173114f9b0b4a06ca6e66ad2ecb65912653c",
            "--to", "bac3431c9bf7c82f57132798da96c37193e50b0a"
        };

        comparison(arguments, true);
        System.out.println();
        System.out.println("Analysis completed.");
    }

    private static void test() throws Exception {
        Process process = new ProcessBuilder("mvn", "test")
            .inheritIO()
            .start();

        int code = process.waitFor();

        if (code != 0) {
            throw new IllegalStateException(
                "Tests failed with exit code " + code
            );
        }
    }
}
