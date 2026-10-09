
package astra.toylang.ast;

import java.util.List;

public class ProgramNode extends ToyNode {

    public final List<ImportNode> imports;
    public final List<FunctionNode> functions;

    public ProgramNode(
            List<ImportNode> imports,
            List<FunctionNode> functions) {

        this.imports = imports;
        this.functions = functions;
    }

    @Override
    public String print(String indent) {

        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("Program\n");

        for (ImportNode node : imports) {
            sb.append(node.print(indent + "  "));
        }

        for (FunctionNode node : functions) {
            sb.append(node.print(indent + "  "));
        }

        return sb.toString();
    }
}
