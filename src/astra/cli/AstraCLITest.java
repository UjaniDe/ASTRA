package astra.cli;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class AstraCLITest {

    @Test
    void versionDisplaysCorrectly() throws Exception {
        String output = capture("--version");
        assertTrue(output.contains("ASTRA v1.0.0"));
    }

    @Test
    void helpDisplaysCommands() throws Exception {
        String output = capture("--help");
        assertTrue(output.contains("analyze"));
        assertTrue(output.contains("compare"));
        assertTrue(output.contains("demo"));
        assertTrue(output.contains("test"));
    }

    @Test
    void helpDisplaysUsage() throws Exception {
        String output = capture("--help");
        assertTrue(output.contains("astra <command>"));
    }

    private String capture(String... args) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(buffer));
            AstraCLI.main(args);
        } finally {
            System.setOut(original);
        }

        return buffer.toString();
    }
}
