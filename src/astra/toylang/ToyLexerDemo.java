
package astra.toylang;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ToyLexerDemo {

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println(
                "Usage: java astra.toylang.ToyLexerDemo <file.toy>"
            );
            return;
        }

        String source = Files.readString(Path.of(args[0]));

        ToyLexer lexer = new ToyLexer(source);

        List<ToyToken> tokens = lexer.tokenize();

        System.out.println("=== TOY LANGUAGE TOKENS ===");

        for (ToyToken token : tokens) {
            System.out.println(token);
        }

        System.out.println("Total tokens: " + tokens.size());
    }
}
