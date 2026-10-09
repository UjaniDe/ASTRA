
package astra.toylang.ast;

public class IdentifierNode extends ExpressionNode {

    public final String name;

    public IdentifierNode(String name) {
        this.name = name;
    }

    @Override
    public String print(String indent) {
        return indent + "Identifier: " + name + "\n";
    }
}
