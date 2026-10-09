
package astra.git;

import astra.compare.ASTComparator;
import astra.compare.StructuralChange;

import astra.toylang.ToyLexer;
import astra.toylang.ToyParser;
import astra.toylang.ast.ProgramNode;

import java.io.IOException;
import java.util.List;

public class GitComparisonService {

    private final String repositoryPath;

    public GitComparisonService(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public List<StructuralChange> compare(
            String oldRevision,
            String newRevision,
            String filePath) throws IOException {

        String oldSource;
        String newSource;

        try (GitSourceLoader loader =
                new GitSourceLoader(repositoryPath)) {

            oldSource = loader.loadFile(
                oldRevision, filePath
            );

            newSource = loader.loadFile(
                newRevision, filePath
            );
        }

        ProgramNode oldAST = parse(oldSource);
        ProgramNode newAST = parse(newSource);

        ASTComparator comparator = new ASTComparator();

        return comparator.compare(oldAST, newAST);
    }

    private ProgramNode parse(String source) {

        ToyLexer lexer = new ToyLexer(source);
        ToyParser parser = new ToyParser(lexer.tokenize());

        return parser.parse();
    }
}
