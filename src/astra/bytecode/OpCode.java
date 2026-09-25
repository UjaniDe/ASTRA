package astra.bytecode;

public enum OpCode {
    PUSH_STRING,     // operand: String  -> push a string literal (e.g. branch name)
    LOAD_COND,       // operand: String  -> look up condition in runtime environment, push boolean
    COMPARE,         // pops two strings, prints the comparison
    ANALYZE_CHANGES, // no operand
    RUN_TESTS,       // no operand
    REPORT,          // no operand
    JUMP,            // operand: Integer -> unconditional jump to instruction index
    JUMP_IF_FALSE,   // operand: Integer -> pop boolean, jump if false
    LABEL,           // operand: String  -> marker only, no-op at runtime
    HALT
}