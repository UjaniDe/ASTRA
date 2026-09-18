package astra;

import astra.lexer.Lexer;
import astra.lexer.Token;
import astra.parser.Parser;
import astra.ast.WorkflowNode;
import astra.semantic.SemanticAnalyzer;

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

        System.out.println("\n=== SEMANTIC ANALYSIS ===");
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(ast);

        System.out.println("\n-- Symbol Table --");
        System.out.print(analyzer.getSymbolTable().print());

        if (!analyzer.getWarnings().isEmpty()) {
            System.out.println("\n-- Warnings --");
            for (String w : analyzer.getWarnings()) System.out.println("WARNING: " + w);
        }

        if (!analyzer.getErrors().isEmpty()) {
            System.out.println("\n-- Errors --");
            for (String e : analyzer.getErrors()) System.out.println("ERROR: " + e);
        }
    }
}