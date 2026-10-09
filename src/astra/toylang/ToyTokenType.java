package astra.toylang;

public enum ToyTokenType {

    // Keywords
    IMPORT,
    FUNCTION,
    IF,
    ELSE,
    RETURN,
    TRUE,
    FALSE,

    // Identifiers and literals
    IDENTIFIER,
    NUMBER,
    STRING,

    // Operators
    ASSIGN,
    EQUAL_EQUAL,
    BANG_EQUAL,
    AND_AND,
    OR_OR,
    BANG,

    // Delimiters
    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    COMMA,
    SEMICOLON,
    DOT,

    EOF
}