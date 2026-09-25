package astra.semantic;

import astra.ast.WorkflowNode;
import astra.lexer.Lexer;
import astra.parser.Parser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SemanticAnalyzerTest {

    private WorkflowNode parse(String source) {
        return new Parser(new Lexer(source).tokenize()).parse();
    }

    @Test
    void buildsSymbolTableFromCompareAndIf() {
        WorkflowNode wf = parse(
            "workflow demo { compare \"main\" with \"feature\" if security_change { report } }");
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(wf);

        SymbolTable table = analyzer.getSymbolTable();
        assertTrue(table.alreadyDeclared("main"));
        assertTrue(table.alreadyDeclared("feature"));
        assertTrue(table.alreadyDeclared("security_change"));
        assertTrue(analyzer.getErrors().isEmpty());
    }

    @Test
    void flagsCompareAgainstSameBranch() {
        WorkflowNode wf = parse("workflow demo { compare \"main\" with \"main\" }");
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(wf);

        assertFalse(analyzer.getErrors().isEmpty());
        assertTrue(analyzer.getErrors().get(0).contains("with itself"));
    }

    @Test
    void flagsEmptyWorkflowBody() {
        WorkflowNode wf = parse("workflow demo { }");
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(wf);

        assertFalse(analyzer.getErrors().isEmpty());
        assertTrue(analyzer.getErrors().get(0).contains("empty body"));
    }

    @Test
    void warnsOnDuplicateConditionCheck() {
        WorkflowNode wf = parse("workflow demo { if flag { report } if flag { run_tests } }");
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(wf);

        assertFalse(analyzer.getWarnings().isEmpty());
        assertTrue(analyzer.getWarnings().get(0).contains("checked more than once"));
    }
}