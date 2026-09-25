package astra.vm;

import astra.bytecode.Instruction;
import astra.bytecode.OpCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class VMTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream captured;

    @BeforeEach
    void redirectStdout() {
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
    }

    @AfterEach
    void restoreStdout() {
        System.setOut(originalOut);
    }

    // if flag { report } else { run_tests } — hand-built, mirrors what
    // BytecodeGenerator actually emits for that shape
    private List<Instruction> ifElseProgram() {
        return List.of(
            new Instruction(OpCode.LOAD_COND, "flag"),
            new Instruction(OpCode.JUMP_IF_FALSE, 4),
            new Instruction(OpCode.REPORT, null),
            new Instruction(OpCode.JUMP, 5),
            new Instruction(OpCode.RUN_TESTS, null),
            new Instruction(OpCode.HALT, null)
        );
    }

    @Test
    void takesTrueBranch() {
        RuntimeEnvironment env = new RuntimeEnvironment();
        env.setCondition("flag", true);
        new VM(ifElseProgram(), env).run();

        String output = captured.toString();
        assertTrue(output.contains("Generating report..."));
        assertFalse(output.contains("Running tests..."));
    }

    @Test
    void takesFalseBranch() {
        RuntimeEnvironment env = new RuntimeEnvironment();
        env.setCondition("flag", false);
        new VM(ifElseProgram(), env).run();

        String output = captured.toString();
        assertTrue(output.contains("Running tests..."));
        assertFalse(output.contains("Generating report..."));
    }

    @Test
    void compareOpcodePrintsBothOperands() {
        RuntimeEnvironment env = new RuntimeEnvironment();
        List<Instruction> program = List.of(
            new Instruction(OpCode.PUSH_STRING, "main"),
            new Instruction(OpCode.PUSH_STRING, "feature"),
            new Instruction(OpCode.COMPARE, null),
            new Instruction(OpCode.HALT, null)
        );
        new VM(program, env).run();

        assertTrue(captured.toString().contains("Comparing branch \"main\" with \"feature\""));
    }

    @Test
    void unsetConditionDefaultsFalseWithWarning() {
        RuntimeEnvironment env = new RuntimeEnvironment();
        assertFalse(env.getCondition("never_set"));
        assertTrue(captured.toString().contains("RUNTIME WARNING"));
    }
}