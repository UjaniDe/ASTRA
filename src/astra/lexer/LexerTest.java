package astra.lexer;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class LexerTest {

    @Test
    void tokenizesKeywordsAndStrings() {
        String source = "workflow test { compare \"main\" with \"feature\" }";
        List<Token> tokens = new Lexer(source).tokenize();

        assertEquals(TokenType.WORKFLOW, tokens.get(0).type);
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type);
        assertEquals("test", tokens.get(1).lexeme);
        assertEquals(TokenType.LBRACE, tokens.get(2).type);
        assertEquals(TokenType.COMPARE, tokens.get(3).type);
        assertEquals(TokenType.STRING, tokens.get(4).type);
        assertEquals("main", tokens.get(4).lexeme);
        assertEquals(TokenType.WITH, tokens.get(5).type);
        assertEquals(TokenType.STRING, tokens.get(6).type);
        assertEquals("feature", tokens.get(6).lexeme);
        assertEquals(TokenType.RBRACE, tokens.get(7).type);
        assertEquals(TokenType.EOF, tokens.get(8).type);
    }

    @Test
    void ignoresCommentsAndWhitespace() {
        String source = "workflow x { // this is a comment\n report }";
        List<Token> tokens = new Lexer(source).tokenize();

        assertEquals(TokenType.WORKFLOW, tokens.get(0).type);
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type);
        assertEquals(TokenType.LBRACE, tokens.get(2).type);
        assertEquals(TokenType.REPORT, tokens.get(3).type);
        assertEquals(TokenType.RBRACE, tokens.get(4).type);
    }

    @Test
    void unterminatedStringThrows() {
        Lexer lexer = new Lexer("compare \"main");
        assertThrows(RuntimeException.class, lexer::tokenize);
    }
}