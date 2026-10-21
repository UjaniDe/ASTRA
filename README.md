# ASTRA

**A Compiler-Driven, Version-Aware Source-Code Analysis System**

ASTRA is a Java project combining compiler design, a custom workflow language,
a stack-based virtual machine, Git integration, and AST-based structural comparison.

Instead of only showing which lines changed, ASTRA classifies structural changes
such as modified conditions, added dependencies, and changed function-call arguments.

## Features

- Custom workflow DSL
- Lexer and recursive-descent parser
- Workflow AST and semantic analysis
- Bytecode generation and stack-based VM
- Toy-language AST parser and structural comparator
- LCS-assisted statement alignment
- JGit integration for comparing revisions
- Rule-based workflow decisions
- Human-readable structural change reports
- Maven test execution
- Optional instruction-level VM tracing
- JUnit unit and integration testing

## Requirements

- Java 17 or newer
- Maven
- Git

## Build and test

    mvn clean test

## Run ASTRA

    mvn compile
    mvn dependency:build-classpath -Dmdep.outputFile=target/runtime-classpath.txt
    CP="target/classes:$(cat target/runtime-classpath.txt)"

    java -cp "$CP" astra.Main examples/sample.ast /path/to/repository src/sample.toy --trace

The workflow specifies the Git revisions. The source-file path is relative
to the Git repository.

## CLI options

- --trace : Show executed bytecode instructions
- --run-tests : Enable Maven test execution
- --test-project <directory> : Choose the Maven project to test

## Example workflow

    workflow analyze_project {
        compare "main" with "feature"
        analyze_changes

        if security_change {
            report
        } else {
            run_tests
        }
    }

## Architecture

Workflow execution:

    ASTRA DSL -> Lexer -> Parser -> AST -> Semantic Analysis -> Bytecode -> VM

Git analysis:

    Git revisions -> JGit -> Toy parser -> Source ASTs -> Comparator -> Changes

The VM connects the two pipelines and selects workflow actions.

## Supported structural changes

- Added or removed dependencies
- Added or removed functions
- Modified conditions
- Added or removed calls
- Modified function-call arguments
- Other supported statement modifications

## Security review flag

The security_change condition is currently true when ASTRA detects a changed
conditional expression. This is a review flag, not proof of a vulnerability.

## Limitations

- Structural analysis currently supports the custom toy source language.
- Comparison is performed on one specified source file at a time.
- Maven execution must be explicitly enabled.
- Reports are currently displayed in the terminal rather than exported.

## Testing

The latest validated run contained 30 passing automated tests.

    mvn clean test

## Reproducible demonstration

Run the full demonstration from the project root:

    bash scripts/demo.sh

The script automatically:
- Runs the automated tests
- Creates a temporary Git repository
- Commits two versions of a toy-language program
- Executes the ASTRA compiler and virtual machine
- Detects three structural changes
- Generates a detailed report
- Displays instruction-by-instruction execution tracing

The temporary repository is automatically removed afterward.


### Automatic changed-file comparison

ASTRA can automatically discover and analyze modified `.toy` files
between the two Git revisions specified in a workflow.

Build the project using `mvn clean test`, then generate the runtime classpath:

    mvn dependency:build-classpath -Dmdep.outputFile=target/runtime-classpath.txt

Run automatic comparison against GitHub:

    CP="target/classes:$(cat target/runtime-classpath.txt)"

    java -cp "$CP" astra.Main examples/github-demo.ast https://github.com/UjaniDe/ASTRA.git --all-changed --trace

In this mode, ASTRA discovers modified `.toy` files, compares their
ASTs, combines structural changes for workflow conditions, and
reports results grouped by filename.

Limitations:

- Only modified `.toy` files are analyzed; added, deleted, and
  renamed files are not currently supported.
- Parsing uses ASTRA's toy-language grammar, not a general-purpose
  Java or Python parser.
- The `security_change` flag indicates a changed condition, not
  a confirmed security vulnerability.

## Live GitHub commit comparison

ASTRA can clone a public GitHub repository and compare a supported
toy-language file between two actual commits.

Build the project:

    mvn clean test
    mvn dependency:build-classpath -Dmdep.outputFile=target/runtime-classpath.txt

Run the GitHub demonstration:

    CP="target/classes:$(cat target/runtime-classpath.txt)"

    java -cp "$CP" astra.Main \
      examples/github-demo.ast \
      https://github.com/UjaniDe/ASTRA.git \
      examples/toylang/github-demo.toy \
      --trace

This demonstration retrieves two committed versions of github-demo.toy
directly from GitHub and detects:

- One added dependency
- One modified condition
- One function-call argument modification

The repository is cloned into a temporary directory and automatically
cleaned up afterward.

Only public GitHub HTTPS repositories and files supported by the
toy-language parser are currently supported.

## Repository

https://github.com/UjaniDe/ASTRA


## ASTRA Command-Line Interface

ASTRA provides a command-line interface for compiling workflows
and comparing Git revisions structurally.

### Build

Run:

    mvn clean test
    mvn dependency:build-classpath -Dmdep.outputFile=target/runtime-classpath.txt

### Available commands

    ./bin/astra --help
    ./bin/astra --version
    ./bin/astra demo
    ./bin/astra test

Compare one file:

    ./bin/astra compare --repo . --from <old> --to <new> --file path/to/file.toy

Analyze all modified toy-language files:

    ./bin/astra analyze --repo . --from <old> --to <new>

Run a workflow:

    ./bin/astra run examples/github-demo.ast --repo . --all-changed

Use `--trace` with analysis commands to display the compiler
pipeline and VM execution details.

### Offline final-review demonstration

From the repository root:

    ./scripts/present.sh

The demonstration uses Git revisions stored in the local ASTRA
repository, so it does not require internet connectivity.

