package astra.bytecode;

import astra.ast.WorkflowNode;
import astra.lexer.Lexer;
import astra.parser.Parser;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BytecodeGeneratorTest {

    private WorkflowNode parse(String source) {
        return new Parser(new Lexer(source).tokenize()).parse();
    }

    @Test
    void generatesStraightLineCode() {
        WorkflowNode wf = parse("workflow demo { compare \"main\" with \"feature\" report }");
        List<Instruction> code = new BytecodeGenerator().generate(wf);

        assertEquals(OpCode.PUSH_STRING, code.get(0).opcode);
        assertEquals("main", code.get(0).operand);
        assertEquals(OpCode.PUSH_STRING, code.get(1).opcode);
        assertEquals("feature", code.get(1).operand);
        assertEquals(OpCode.COMPARE, code.get(2).opcode);
        assertEquals(OpCode.REPORT, code.get(3).opcode);
        assertEquals(OpCode.HALT, code.get(4).opcode);
    }

    @Test
    void resolvesIfElseJumpTargetsToRealIndices() {
        WorkflowNode wf = parse("workflow demo { if cond { report } else { run_tests } }");
        List<Instruction> code = new BytecodeGenerator().generate(wf);

        assertEquals(OpCode.LOAD_COND, code.get(0).opcode);
        assertEquals(OpCode.JUMP_IF_FALSE, code.get(1).opcode);
        assertTrue(code.get(1).operand instanceof Integer, "label must resolve to an int index");

        int elseTarget = (Integer) code.get(1).operand;
        assertEquals(OpCode.LABEL, code.get(elseTarget).opcode);
        assertEquals(OpCode.RUN_TESTS, code.get(elseTarget + 1).opcode);

        assertEquals(OpCode.REPORT, code.get(2).opcode);
        assertEquals(OpCode.JUMP, code.get(3).opcode);
        int endTarget = (Integer) code.get(3).operand;
        assertEquals(OpCode.LABEL, code.get(endTarget).opcode);
    }

    @Test
    void everyJumpOperandIsResolvedNotSymbolic() {
        WorkflowNode wf = parse("workflow demo { if cond { report } }");
        List<Instruction> code = new BytecodeGenerator().generate(wf);
        for (Instruction instr : code) {
            if (instr.opcode == OpCode.JUMP || instr.opcode == OpCode.JUMP_IF_FALSE) {
                assertTrue(instr.operand instanceof Integer);
            }
        }
    }
}