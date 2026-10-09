
package astra.compare;

import astra.toylang.ToyLexer;
import astra.toylang.ToyParser;
import astra.toylang.ast.ProgramNode;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ASTComparatorTest {

    private ProgramNode parse(String source) {
        return new ToyParser(
            new ToyLexer(source).tokenize()
        ).parse();
    }

    private List<StructuralChange> compare(
            String before,
            String after) {

        ASTComparator comparator = new ASTComparator();

        return comparator.compare(
            parse(before),
            parse(after)
        );
    }

    @Test
    void identicalProgramsHaveNoChanges() {
        String source =
            "function demo() { log(); }";

        assertTrue(compare(source, source).isEmpty());
    }

    @Test
    void detectsAddedDependency() {
        String before = "import auth;";
        String after = "import auth; import audit;";

        List<StructuralChange> changes =
            compare(before, after);

        assertEquals(1, changes.size());
        assertEquals(
            ChangeType.DEPENDENCY_ADDED,
            changes.get(0).type
        );
        assertEquals("audit", changes.get(0).after);
    }

    @Test
    void detectsConditionChange() {
        String before =
            "function check(user) {"
            + " if (user.isAdmin) { return true; }"
            + "}";

        String after =
            "function check(user) {"
            + " if (user.isAdmin || user.isGuest)"
            + " { return true; }"
            + "}";

        List<StructuralChange> changes =
            compare(before, after);

        assertEquals(1, changes.size());
        assertEquals(
            ChangeType.CONDITION_CHANGED,
            changes.get(0).type
        );
    }

    @Test
    void detectsArgumentChange() {
        String before =
            "function demo() { log(1); }";

        String after =
            "function demo() { log(1, 2); }";

        List<StructuralChange> changes =
            compare(before, after);

        assertEquals(1, changes.size());
        assertEquals(
            ChangeType.ARGUMENTS_CHANGED,
            changes.get(0).type
        );
    }

    @Test
    void detectsInsertedCallWithoutFalseChanges() {
        String before =
            "function demo() {"
            + " validate(); log();"
            + "}";

        String after =
            "function demo() {"
            + " audit(); validate(); log();"
            + "}";

        List<StructuralChange> changes =
            compare(before, after);

        assertEquals(1, changes.size());
        assertEquals(
            ChangeType.CALL_ADDED,
            changes.get(0).type
        );
        assertEquals("audit()", changes.get(0).after);
    }

    @Test
    void detectsRemovedFunction() {
        String before =
            "function first() {} function second() {}";

        String after =
            "function first() {}";

        List<StructuralChange> changes =
            compare(before, after);

        assertEquals(1, changes.size());
        assertEquals(
            ChangeType.FUNCTION_REMOVED,
            changes.get(0).type
        );
    }

    @Test
    void detectsAddedFunction() {
        String before =
            "function first() {}";

        String after =
            "function first() {} function second() {}";

        List<StructuralChange> changes =
            compare(before, after);

        assertEquals(1, changes.size());
        assertEquals(
            ChangeType.FUNCTION_ADDED,
            changes.get(0).type
        );
    }

    @Test
    void detectsRemovedDependency() {
        List<StructuralChange> changes =
            compare("import auth;", "");

        assertEquals(1, changes.size());
        assertEquals(
            ChangeType.DEPENDENCY_REMOVED,
            changes.get(0).type
        );
    }
}
