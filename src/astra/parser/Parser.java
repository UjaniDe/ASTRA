package astra.parser;

import astra.lexer.Token;
import astra.lexer.TokenType;
import astra.ast.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public WorkflowNode parse() {
        return parseWorkflow();
    }

    private WorkflowNode parseWorkflow() {
        consume(TokenType.WORKFLOW, "Expected 'workflow'");
        Token name = consume(TokenType.IDENTIFIER, "Expected workflow name");
        consume(TokenType.LBRACE, "Expected '{'");
        List<Node> statements = parseStatements();
        consume(TokenType.RBRACE, "Expected '}'");
        return new WorkflowNode(name.lexeme, statements);
    }

    private List<Node> parseStatements() {
        List<Node> statements = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            statements.add(parseStatement());
        }
        return statements;
    }

    private Node parseStatement() {
        if (match(TokenType.COMPARE)) return parseCompare();
        if (match(TokenType.ANALYZE_CHANGES)) return new AnalyzeNode();
        if (match(TokenType.RUN_TESTS)) return new RunTestsNode();
        if (match(TokenType.REPORT)) return new ReportNode();
        if (match(TokenType.IF)) return parseIf();
        throw error("Unexpected token: " + peek());
    }

    private Node parseCompare() {
        Token a = consume(TokenType.STRING, "Expected string after 'compare'");
        consume(TokenType.WITH, "Expected 'with'");
        Token b = consume(TokenType.STRING, "Expected string after 'with'");
        return new CompareNode(a.lexeme, b.lexeme);
    }

    private Node parseIf() {
        Token cond = consume(TokenType.IDENTIFIER, "Expected condition name after 'if'");
        consume(TokenType.LBRACE, "Expected '{'");
        List<Node> thenBranch = parseStatements();
        consume(TokenType.RBRACE, "Expected '}'");

        List<Node> elseBranch = null;
        if (match(TokenType.ELSE)) {
            consume(TokenType.LBRACE, "Expected '{' after else");
            elseBranch = parseStatements();
            consume(TokenType.RBRACE, "Expected '}'");
        }
        return new IfNode(cond.lexeme, thenBranch, elseBranch);
    }

    // --- helpers ---
    private boolean match(TokenType type) {
        if (check(type)) { advance(); return true; }
        return false;
    }

    private boolean check(TokenType type) {
        return peek().type == type;
    }

    private Token advance() {
        return tokens.get(pos++);
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(message + " (got " + peek() + " at line " + peek().line + ")");
    }

    private RuntimeException error(String message) {
        return new RuntimeException("Parse error: " + message);
    }
}