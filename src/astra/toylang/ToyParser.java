
package astra.toylang;

import astra.toylang.ast.*;

import java.util.ArrayList;
import java.util.List;

public class ToyParser {

    private final List<ToyToken> tokens;
    private int pos = 0;

    public ToyParser(List<ToyToken> tokens) {
        this.tokens = tokens;
    }

    // ==============================
    // PROGRAM
    // ==============================

    public ProgramNode parse() {

        List<ImportNode> imports = new ArrayList<>();
        List<FunctionNode> functions = new ArrayList<>();

        while (!check(ToyTokenType.EOF)) {

            if (match(ToyTokenType.IMPORT)) {
                imports.add(parseImport());

            } else if (match(ToyTokenType.FUNCTION)) {
                functions.add(parseFunction());

            } else {
                throw error(
                    "Expected 'import' or 'function'"
                );
            }
        }

        return new ProgramNode(imports, functions);
    }

    // ==============================
    // IMPORT
    // ==============================

    private ImportNode parseImport() {

        ToyToken name = consume(
            ToyTokenType.IDENTIFIER,
            "Expected dependency name after 'import'"
        );

        consume(
            ToyTokenType.SEMICOLON,
            "Expected ';' after import"
        );

        return new ImportNode(name.lexeme);
    }

    // ==============================
    // FUNCTION
    // ==============================

    private FunctionNode parseFunction() {

        ToyToken name = consume(
            ToyTokenType.IDENTIFIER,
            "Expected function name"
        );

        consume(
            ToyTokenType.LPAREN,
            "Expected '(' after function name"
        );

        List<String> parameters = new ArrayList<>();

        if (!check(ToyTokenType.RPAREN)) {

            do {
                ToyToken parameter = consume(
                    ToyTokenType.IDENTIFIER,
                    "Expected parameter name"
                );

                parameters.add(parameter.lexeme);

            } while (match(ToyTokenType.COMMA));
        }

        consume(
            ToyTokenType.RPAREN,
            "Expected ')' after function parameters"
        );

        consume(
            ToyTokenType.LBRACE,
            "Expected '{' before function body"
        );

        List<StatementNode> body = parseStatements();

        consume(
            ToyTokenType.RBRACE,
            "Expected '}' after function body"
        );

        return new FunctionNode(
            name.lexeme,
            parameters,
            body
        );
    }

    // ==============================
    // STATEMENTS
    // ==============================

    private List<StatementNode> parseStatements() {

        List<StatementNode> statements = new ArrayList<>();

        while (!check(ToyTokenType.RBRACE)
                && !check(ToyTokenType.EOF)) {

            statements.add(parseStatement());
        }

        return statements;
    }

    private StatementNode parseStatement() {

        if (match(ToyTokenType.IF)) {
            return parseIf();
        }

        if (match(ToyTokenType.RETURN)) {
            return parseReturn();
        }

        if (check(ToyTokenType.IDENTIFIER)) {
            return parseAssignmentOrCall();
        }

        throw error(
            "Unexpected token in statement: " + peek()
        );
    }

    // ==============================
    // IF / ELSE
    // ==============================

    private IfNode parseIf() {

        consume(
            ToyTokenType.LPAREN,
            "Expected '(' after 'if'"
        );

        ExpressionNode condition = parseExpression();

        consume(
            ToyTokenType.RPAREN,
            "Expected ')' after condition"
        );

        consume(
            ToyTokenType.LBRACE,
            "Expected '{' before if body"
        );

        List<StatementNode> thenBranch = parseStatements();

        consume(
            ToyTokenType.RBRACE,
            "Expected '}' after if body"
        );

        List<StatementNode> elseBranch = null;

        if (match(ToyTokenType.ELSE)) {

            consume(
                ToyTokenType.LBRACE,
                "Expected '{' after 'else'"
            );

            elseBranch = parseStatements();

            consume(
                ToyTokenType.RBRACE,
                "Expected '}' after else body"
            );
        }

        return new IfNode(
            condition,
            thenBranch,
            elseBranch
        );
    }

    // ==============================
    // RETURN
    // ==============================

    private ReturnNode parseReturn() {

        ExpressionNode value = null;

        if (!check(ToyTokenType.SEMICOLON)) {
            value = parseExpression();
        }

        consume(
            ToyTokenType.SEMICOLON,
            "Expected ';' after return"
        );

        return new ReturnNode(value);
    }

    // ==============================
    // ASSIGNMENT / FUNCTION CALL
    // ==============================

    private StatementNode parseAssignmentOrCall() {

        ExpressionNode expression = parsePostfix();

        if (match(ToyTokenType.ASSIGN)) {

            if (!(expression instanceof IdentifierNode)) {
                throw error(
                    "Assignment target must be an identifier"
                );
            }

            String name = ((IdentifierNode) expression).name;

            ExpressionNode value = parseExpression();

            consume(
                ToyTokenType.SEMICOLON,
                "Expected ';' after assignment"
            );

            return new AssignNode(name, value);
        }

        if (expression instanceof CallExpressionNode) {

            consume(
                ToyTokenType.SEMICOLON,
                "Expected ';' after function call"
            );

            return new CallStatementNode(
                (CallExpressionNode) expression
            );
        }

        throw error(
            "Expected assignment or function call"
        );
    }

    // ==============================
    // EXPRESSIONS
    // ==============================

    private ExpressionNode parseExpression() {
        return parseOr();
    }

    // Logical OR: ||

    private ExpressionNode parseOr() {

        ExpressionNode left = parseAnd();

        while (match(ToyTokenType.OR_OR)) {

            ExpressionNode right = parseAnd();

            left = new BinaryExpressionNode(
                left,
                "||",
                right
            );
        }

        return left;
    }

    // Logical AND: &&

    private ExpressionNode parseAnd() {

        ExpressionNode left = parseEquality();

        while (match(ToyTokenType.AND_AND)) {

            ExpressionNode right = parseEquality();

            left = new BinaryExpressionNode(
                left,
                "&&",
                right
            );
        }

        return left;
    }

    // Equality: == and !=

    private ExpressionNode parseEquality() {

        ExpressionNode left = parseUnary();

        while (true) {

            if (match(ToyTokenType.EQUAL_EQUAL)) {

                ExpressionNode right = parseUnary();

                left = new BinaryExpressionNode(
                    left,
                    "==",
                    right
                );

            } else if (match(ToyTokenType.BANG_EQUAL)) {

                ExpressionNode right = parseUnary();

                left = new BinaryExpressionNode(
                    left,
                    "!=",
                    right
                );

            } else {
                break;
            }
        }

        return left;
    }

    // Unary NOT: !

    private ExpressionNode parseUnary() {

        if (match(ToyTokenType.BANG)) {

            ExpressionNode operand = parseUnary();

            return new UnaryExpressionNode(
                "!",
                operand
            );
        }

        return parsePostfix();
    }

    // ==============================
    // MEMBER ACCESS AND CALLS
    // ==============================

    private ExpressionNode parsePostfix() {

        ExpressionNode expression = parsePrimary();

        while (true) {

            // Member access: user.isAdmin

            if (match(ToyTokenType.DOT)) {

                ToyToken member = consume(
                    ToyTokenType.IDENTIFIER,
                    "Expected member name after '.'"
                );

                expression = new MemberAccessNode(
                    expression,
                    member.lexeme
                );

            // Function call: check(user)

            } else if (match(ToyTokenType.LPAREN)) {

                List<ExpressionNode> arguments =
                    new ArrayList<>();

                if (!check(ToyTokenType.RPAREN)) {

                    do {
                        arguments.add(parseExpression());

                    } while (match(ToyTokenType.COMMA));
                }

                consume(
                    ToyTokenType.RPAREN,
                    "Expected ')' after arguments"
                );

                expression = new CallExpressionNode(
                    expression,
                    arguments
                );

            } else {
                break;
            }
        }

        return expression;
    }

    // ==============================
    // PRIMARY EXPRESSIONS
    // ==============================

    private ExpressionNode parsePrimary() {

        if (match(ToyTokenType.TRUE)) {
            return new LiteralNode(true);
        }

        if (match(ToyTokenType.FALSE)) {
            return new LiteralNode(false);
        }

        if (match(ToyTokenType.NUMBER)) {

            ToyToken token = previous();

            try {
                return new LiteralNode(
                    Integer.parseInt(token.lexeme)
                );
            } catch (NumberFormatException e) {
                throw error(
                    "Invalid integer literal: " + token.lexeme
                );
            }
        }

        if (match(ToyTokenType.STRING)) {
            return new LiteralNode(previous().lexeme);
        }

        if (match(ToyTokenType.IDENTIFIER)) {
            return new IdentifierNode(previous().lexeme);
        }

        if (match(ToyTokenType.LPAREN)) {

            ExpressionNode expression = parseExpression();

            consume(
                ToyTokenType.RPAREN,
                "Expected ')' after expression"
            );

            return expression;
        }

        throw error(
            "Expected expression, found " + peek()
        );
    }

    // ==============================
    // PARSER HELPERS
    // ==============================

    private boolean match(ToyTokenType type) {

        if (check(type)) {
            pos++;
            return true;
        }

        return false;
    }

    private boolean check(ToyTokenType type) {
        return peek().type == type;
    }

    private ToyToken consume(
            ToyTokenType type,
            String message) {

        if (check(type)) {
            return tokens.get(pos++);
        }

        throw error(message);
    }

    private ToyToken peek() {
        return tokens.get(pos);
    }

    private ToyToken previous() {
        return tokens.get(pos - 1);
    }

    private RuntimeException error(String message) {

        ToyToken token = peek();

        return new RuntimeException(
            "Toy parse error at line "
                + token.line
                + ": "
                + message
        );
    }
}
