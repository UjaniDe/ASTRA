package astra;

import astra.lexer.Lexer;
import astra.lexer.Token;
import astra.parser.Parser;
import astra.ast.WorkflowNode;
import astra.semantic.SemanticAnalyzer;
import astra.bytecode.BytecodeGenerator;
import astra.bytecode.Instruction;
import astra.vm.RuntimeEnvironment;
import astra.vm.VM;

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
        WorkflowNode ast;
        try {
            ast = parser.parse();
        } catch (RuntimeException e) {
            System.out.println("\n=== PARSE ERROR ===");
            System.out.println(e.getMessage());
            return;
        }
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
            System.out.println("\nCompilation halted due to semantic errors — skipping bytecode generation.");
            return;
        }

        System.out.println("\n=== BYTECODE ===");
        BytecodeGenerator generator = new BytecodeGenerator();
        List<Instruction> bytecode = generator.generate(ast);
        System.out.print(BytecodeGenerator.print(bytecode));

        System.out.println("\n=== VM EXECUTION ===");
        RuntimeEnvironment env = new RuntimeEnvironment();
        // Demo condition values — Phase 3 will populate these from real Git-based analysis
      env.setCondition("security_change", true);

        VM vm = new VM(bytecode, env);
        vm.run();
    }
}