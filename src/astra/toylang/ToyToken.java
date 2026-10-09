package astra.toylang;

public class ToyToken {

    public final ToyTokenType type;
    public final String lexeme;
    public final int line;

    public ToyToken(
            ToyTokenType type,
            String lexeme,
            int line) {

        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    @Override
    public String toString() {
        return type + "(" + lexeme + ") at line " + line;
    }
}