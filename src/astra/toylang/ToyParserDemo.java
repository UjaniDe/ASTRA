
package astra.toylang;

import astra.toylang.ast.ProgramNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ToyParserDemo {

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println(
                "Usage: java astra.toylang.ToyParserDemo <file.toy>"
            );
            return;
        }

        String source = Files.readString(Path.of(args[0]));

        ToyLexer lexer = new ToyLexer(source);
        List<ToyToken> tokens = lexer.tokenize();

        ToyParser parser = new ToyParser(tokens);
        ProgramNode ast = parser.parse();

        System.out.println("=== TOY LANGUAGE AST ===");
        System.out.print(ast.print(""));
    }
}
