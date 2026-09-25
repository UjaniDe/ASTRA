package astra.vm;

import astra.bytecode.Instruction;
import astra.bytecode.OpCode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class VM {
    private final List<Instruction> program;
    private final RuntimeEnvironment env;
    private final Deque<Object> stack = new ArrayDeque<>();
    private int ip = 0; // instruction pointer

    public VM(List<Instruction> program, RuntimeEnvironment env) {
        this.program = program;
        this.env = env;
    }

    public void run() {
        while (ip < program.size()) {
            Instruction instr = program.get(ip);

            switch (instr.opcode) {
                case PUSH_STRING -> {
                    stack.push(instr.operand);
                    ip++;
                }
                case LOAD_COND -> {
                    boolean value = env.getCondition((String) instr.operand);
                    stack.push(value);
                    ip++;
                }
                case COMPARE -> {
                    Object b = stack.pop();
                    Object a = stack.pop();
                    System.out.println("Comparing branch \"" + a + "\" with \"" + b + "\"");
                    ip++;
                }
                case ANALYZE_CHANGES -> {
                    System.out.println("Analyzing structural changes...");
                    ip++;
                }
                case RUN_TESTS -> {
                    System.out.println("Running tests...");
                    ip++;
                }
                case REPORT -> {
                    System.out.println("Generating report...");
                    ip++;
                }
                case JUMP -> ip = (Integer) instr.operand;
                case JUMP_IF_FALSE -> {
                    boolean cond = (Boolean) stack.pop();
                    if (!cond) {
                        ip = (Integer) instr.operand;
                    } else {
                        ip++;
                    }
                }
                case LABEL -> ip++; // no-op marker
                case HALT -> {
                    return;
                }
                default -> throw new RuntimeException("VM error: unknown opcode " + instr.opcode);
            }
        }
    }
}