package astra.toylang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ToyLexer {

    private final String source;
    private final List<ToyToken> tokens = new ArrayList<>();

    private int pos = 0;
    private int line = 1;

    private static final Map<String, ToyTokenType> KEYWORDS =
        Map.of(
            "import", ToyTokenType.IMPORT,
            "function", ToyTokenType.FUNCTION,
            "if", ToyTokenType.IF,
            "else", ToyTokenType.ELSE,
            "return", ToyTokenType.RETURN,
            "true", ToyTokenType.TRUE,
            "false", ToyTokenType.FALSE
        );

    public ToyLexer(String source) {
        this.source = source;
    }

    public List<ToyToken> tokenize() {

        while (!isAtEnd()) {

            char c = peek();

            if (Character.isWhitespace(c)) {
                if (c == '\n') line++;
                advance();
                continue;
            }

            if (c == '/' && peekNext() == '/') {
                while (!isAtEnd() && peek() != '\n') {
                    advance();
                }
                continue;
            }

            if (Character.isLetter(c) || c == '_') {
                readIdentifier();
                continue;
            }

            if (Character.isDigit(c)) {
                readNumber();
                continue;
            }

            if (c == '"') {
                readString();
                continue;
            }

            switch (c) {

                case '(' -> addSingle(ToyTokenType.LPAREN);
                case ')' -> addSingle(ToyTokenType.RPAREN);

                case '{' -> addSingle(ToyTokenType.LBRACE);
                case '}' -> addSingle(ToyTokenType.RBRACE);

                case ',' -> addSingle(ToyTokenType.COMMA);
                case ';' -> addSingle(ToyTokenType.SEMICOLON);
                case '.' -> addSingle(ToyTokenType.DOT);

                case '=' -> readOperator(
                    '=',
                    ToyTokenType.ASSIGN,
                    ToyTokenType.EQUAL_EQUAL
                );

                case '!' -> readOperator(
                    '=',
                    ToyTokenType.BANG,
                    ToyTokenType.BANG_EQUAL
                );

                case '&' -> readDoubleOperator(
                    '&', ToyTokenType.AND_AND
                );

                case '|' -> readDoubleOperator(
                    '|', ToyTokenType.OR_OR
                );

                default -> throw new RuntimeException(
                    "Unexpected character '" + c +
                    "' at line " + line
                );
            }
        }

        tokens.add(new ToyToken(
            ToyTokenType.EOF, "", line
        ));

        return tokens;
    }

    private void readIdentifier() {

        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() &&
                (Character.isLetterOrDigit(peek())
                 || peek() == '_')) {

            sb.append(advance());
        }

        String word = sb.toString();

        ToyTokenType type = KEYWORDS.getOrDefault(
            word, ToyTokenType.IDENTIFIER
        );

        tokens.add(new ToyToken(type, word, line));
    }

    private void readNumber() {

        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() && Character.isDigit(peek())) {
            sb.append(advance());
        }

        tokens.add(new ToyToken(
            ToyTokenType.NUMBER,
            sb.toString(),
            line
        ));
    }

    private void readString() {

        int startLine = line;
        advance();

        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() && peek() != '"') {

            if (peek() == '\n') line++;
            sb.append(advance());
        }

        if (isAtEnd()) {
            throw new RuntimeException(
                "Unterminated string at line " + startLine
            );
        }

        advance();

        tokens.add(new ToyToken(
            ToyTokenType.STRING,
            sb.toString(),
            startLine
        ));
    }

    private void readOperator(
            char second,
            ToyTokenType single,
            ToyTokenType combined) {

        advance();

        if (!isAtEnd() && peek() == second) {
            advance();
            tokens.add(new ToyToken(combined,
                combined == ToyTokenType.EQUAL_EQUAL
                    ? "==" : "!=", line));
        } else {
            tokens.add(new ToyToken(
                single,
                single == ToyTokenType.ASSIGN ? "=" : "!",
                line
            ));
        }
    }

    private void readDoubleOperator(
            char expected,
            ToyTokenType type) {

        advance();

        if (isAtEnd() || peek() != expected) {
            throw new RuntimeException(
                "Expected '" + expected + expected +
                "' at line " + line
            );
        }

        advance();

        tokens.add(new ToyToken(
            type, "" + expected + expected, line
        ));
    }

    private void addSingle(ToyTokenType type) {

        String lexeme = String.valueOf(advance());

        tokens.add(new ToyToken(type, lexeme, line));
    }

    private boolean isAtEnd() {
        return pos >= source.length();
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(pos);
    }

    private char peekNext() {
        return pos + 1 >= source.length()
            ? '\0' : source.charAt(pos + 1);
    }

    private char advance() {
        return source.charAt(pos++);
    }
}