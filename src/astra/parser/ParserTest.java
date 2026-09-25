package astra.parser;

import astra.ast.*;
import astra.lexer.Lexer;
import astra.lexer.Token;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    private WorkflowNode parse(String source) {
        List<Token> tokens = new Lexer(source).tokenize();
        return new Parser(tokens).parse();
    }

    @Test
    void parsesSimpleWorkflow() {
        WorkflowNode wf = parse("workflow demo { compare \"main\" with \"feature\" report }");
        assertEquals("demo", wf.name);
        assertEquals(2, wf.statements.size());
        assertTrue(wf.statements.get(0) instanceof CompareNode);
        assertTrue(wf.statements.get(1) instanceof ReportNode);
    }

    @Test
    void parsesIfElse() {
        WorkflowNode wf = parse("workflow demo { if flag { report } else { run_tests } }");
        IfNode ifNode = (IfNode) wf.statements.get(0);
        assertEquals("flag", ifNode.condition);
        assertTrue(ifNode.thenBranch.get(0) instanceof ReportNode);
        assertNotNull(ifNode.elseBranch);
        assertTrue(ifNode.elseBranch.get(0) instanceof RunTestsNode);
    }

    @Test
    void missingClosingBraceThrows() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> parse("workflow demo { report "));
        assertTrue(ex.getMessage().contains("Expected '}'"));
    }

    @Test
    void missingWorkflowNameThrows() {
        assertThrows(RuntimeException.class, () -> parse("workflow { report }"));
    }
}