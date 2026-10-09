
package astra.compare;

import astra.toylang.ToyLexer;
import astra.toylang.ToyParser;
import astra.toylang.ast.ProgramNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ASTComparatorDemo {

    private static ProgramNode parseFile(
            String filename) throws Exception {

        String source = Files.readString(Path.of(filename));

        ToyLexer lexer = new ToyLexer(source);
        ToyParser parser = new ToyParser(lexer.tokenize());

        return parser.parse();
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.println(
                "Usage: ASTComparatorDemo <old.toy> <new.toy>"
            );
            return;
        }

        ProgramNode before = parseFile(args[0]);
        ProgramNode after = parseFile(args[1]);

        ASTComparator comparator = new ASTComparator();

        List<StructuralChange> changes =
            comparator.compare(before, after);

        System.out.println(
            "=== ASTRA STRUCTURAL ANALYSIS ==="
        );

        if (changes.isEmpty()) {
            System.out.println("No structural changes detected.");
            return;
        }

        for (StructuralChange change : changes) {
            System.out.println();
            System.out.print(change);
        }

        System.out.println(
            "\nTotal structural changes: " + changes.size()
        );
    }
}
