package astra.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String source;
    private int pos = 0;
    private int line = 1;
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> KEYWORDS = Map.of(
        "workflow", TokenType.WORKFLOW,
        "compare", TokenType.COMPARE,
        "with", TokenType.WITH,
        "analyze_changes", TokenType.ANALYZE_CHANGES,
        "run_tests", TokenType.RUN_TESTS,
        "report", TokenType.REPORT,
        "if", TokenType.IF,
        "else", TokenType.ELSE
    );

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            skipWhitespaceAndComments();
            if (isAtEnd()) break;

            char c = peek();

            if (c == '{') { advance(); tokens.add(new Token(TokenType.LBRACE, "{", line)); }
            else if (c == '}') { advance(); tokens.add(new Token(TokenType.RBRACE, "}", line)); }
            else if (c == '"') { readString(); }
            else if (isAlpha(c)) { readIdentifierOrKeyword(); }
            else {
                throw new RuntimeException("Unexpected character '" + c + "' at line " + line);
            }
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private void readString() {
        advance(); // consume opening quote
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') line++;
            sb.append(advance());
        }
        if (isAtEnd()) throw new RuntimeException("Unterminated string at line " + line);
        advance(); // consume closing quote
        tokens.add(new Token(TokenType.STRING, sb.toString(), line));
    }

    private void readIdentifierOrKeyword() {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && (isAlpha(peek()) || isDigit(peek()) || peek() == '_')) {
            sb.append(advance());
        }
        String word = sb.toString();
        TokenType type = KEYWORDS.getOrDefault(word, TokenType.IDENTIFIER);
        tokens.add(new Token(type, word, line));
    }

    private void skipWhitespaceAndComments() {
        while (!isAtEnd()) {
            char c = peek();
            if (c == ' ' || c == '\r' || c == '\t') { advance(); }
            else if (c == '\n') { line++; advance(); }
            else if (c == '/' && peekNext() == '/') {
                while (!isAtEnd() && peek() != '\n') advance();
            } else {
                break;
            }
        }
    }

    private boolean isAlpha(char c) { return Character.isLetter(c); }
    private boolean isDigit(char c) { return Character.isDigit(c); }
    private boolean isAtEnd() { return pos >= source.length(); }
    private char peek() { return isAtEnd() ? '\0' : source.charAt(pos); }
    private char peekNext() { return pos + 1 >= source.length() ? '\0' : source.charAt(pos + 1); }
    private char advance() { return source.charAt(pos++); }
}