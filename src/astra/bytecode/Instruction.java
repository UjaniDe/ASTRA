package astra.bytecode;

public class Instruction {
    public final OpCode opcode;
    public Object operand; // String, Integer, or null depending on opcode

    public Instruction(OpCode opcode, Object operand) {
        this.opcode = opcode;
        this.operand = operand;
    }

    @Override
    public String toString() {
        return operand == null ? opcode.toString() : opcode + " " + operand;
    }
}