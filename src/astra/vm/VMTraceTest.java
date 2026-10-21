package astra.vm;

import astra.bytecode.Instruction;
import astra.bytecode.OpCode;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VMTraceTest {

    private List<Instruction> program() {
        return List.of(
            new Instruction(OpCode.PUSH_STRING, "example"),
            new Instruction(OpCode.HALT, null)
        );
    }

    private String executeWithTrace(boolean enabled) {
        RuntimeEnvironment env = new RuntimeEnvironment();
        env.setTraceEnabled(enabled);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.out;

        try {
            System.setOut(new PrintStream(buffer));
            new VM(program(), env).run();
        } finally {
            System.setOut(original);
        }

        return buffer.toString();
    }

    @Test
    void traceShowsExecutedInstructions() {
        String output = executeWithTrace(true);

        assertTrue(output.contains("[TRACE] IP=0  PUSH_STRING example"));
        assertTrue(output.contains("[TRACE] IP=1  HALT"));
    }

    @Test
    void traceIsDisabledByDefault() {
        String output = executeWithTrace(false);

        assertFalse(output.contains("[TRACE]"));
    }
}
