package astra.bytecode;

import astra.ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BytecodeGenerator {
    private final List<Instruction> instructions = new ArrayList<>();
    private int labelCounter = 0;

    public List<Instruction> generate(WorkflowNode workflow) {
        for (Node stmt : workflow.statements) {
            emit(stmt);
        }
        instructions.add(new Instruction(OpCode.HALT, null));
        resolveLabels();
        return instructions;
    }

    private void emit(Node node) {
        if (node instanceof CompareNode compare) {
            instructions.add(new Instruction(OpCode.PUSH_STRING, compare.branchA));
            instructions.add(new Instruction(OpCode.PUSH_STRING, compare.branchB));
            instructions.add(new Instruction(OpCode.COMPARE, null));
        } else if (node instanceof AnalyzeNode) {
            instructions.add(new Instruction(OpCode.ANALYZE_CHANGES, null));
        } else if (node instanceof RunTestsNode) {
            instructions.add(new Instruction(OpCode.RUN_TESTS, null));
        } else if (node instanceof ReportNode) {
            instructions.add(new Instruction(OpCode.REPORT, null));
        } else if (node instanceof IfNode ifNode) {
            emitIf(ifNode);
        }
    }

    private void emitIf(IfNode ifNode) {
        String elseLabel = newLabel();
        String endLabel = newLabel();

        instructions.add(new Instruction(OpCode.LOAD_COND, ifNode.condition));
        instructions.add(new Instruction(OpCode.JUMP_IF_FALSE, elseLabel)); // resolved later

        for (Node stmt : ifNode.thenBranch) emit(stmt);
        instructions.add(new Instruction(OpCode.JUMP, endLabel));

        instructions.add(new Instruction(OpCode.LABEL, elseLabel));
        if (ifNode.elseBranch != null) {
            for (Node stmt : ifNode.elseBranch) emit(stmt);
        }

        instructions.add(new Instruction(OpCode.LABEL, endLabel));
    }

    private String newLabel() {
        return "L" + (labelCounter++);
    }

    // Second pass: turn symbolic label names into actual instruction indices
    private void resolveLabels() {
        Map<String, Integer> labelPositions = new HashMap<>();
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instr = instructions.get(i);
            if (instr.opcode == OpCode.LABEL) {
                labelPositions.put((String) instr.operand, i);
            }
        }
        for (Instruction instr : instructions) {
            if (instr.opcode == OpCode.JUMP || instr.opcode == OpCode.JUMP_IF_FALSE) {
                String label = (String) instr.operand;
                Integer target = labelPositions.get(label);
                if (target == null) {
                    throw new RuntimeException("Bytecode error: undefined label '" + label + "'");
                }
                instr.operand = target;
            }
        }
    }

    public static String print(List<Instruction> instructions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < instructions.size(); i++) {
            sb.append(String.format("%3d: %s%n", i, instructions.get(i)));
        }
        return sb.toString();
    }
}