package astra;

import astra.lexer.Lexer;
import astra.lexer.Token;
import astra.parser.Parser;
import astra.ast.WorkflowNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java astra.Main <file.ast>");
            return;
        }
        String source = Files.readString(Path.of(args[0]));

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        System.out.println("=== TOKENS ===");
        for (Token t : tokens) System.out.println(t);

        Parser parser = new Parser(tokens);
        WorkflowNode ast = parser.parse();
        System.out.println("\n=== AST ===");
        System.out.print(ast.print(""));
    }
}